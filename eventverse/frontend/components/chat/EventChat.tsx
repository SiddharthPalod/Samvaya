"use client";
import { useEffect, useRef, useState, useCallback } from "react";
import { useAuth } from "@/context/AuthContext";
import api from "@/lib/api";
import { Send, Users, ChevronUp } from "lucide-react";

interface ChatMessage {
  id: string;
  roomId: string;
  senderId: string;
  senderEmail?: string;
  content: string;
  timestamp: number;
}

interface TypingIndicator {
  userId: string;
  roomId: string;
  isTyping: boolean;
  timestamp: number;
}

interface ReadReceipt {
  messageId: string;
  userId: string;
  roomId: string;
  readAt: number;
}

interface EventChatProps {
  eventId: string;
}

// Extract name from email and capitalize it
function extractAndCapitalizeName(email: string): string {
  if (!email) return email;
  
  // Get the part before @
  const localPart = email.split('@')[0];
  
  // Replace dots, underscores, and hyphens with spaces
  const nameWithSpaces = localPart.replace(/[._-]/g, ' ');
  
  // Capitalize each word
  const capitalized = nameWithSpaces
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(' ');
  
  return capitalized;
}

export default function EventChat({ eventId }: EventChatProps) {
  const { user } = useAuth();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [newMessage, setNewMessage] = useState("");
  const [onlineUsers, setOnlineUsers] = useState<number>(0);
  const [typingUsers, setTypingUsers] = useState<Set<string>>(new Set());
  const [readReceipts, setReadReceipts] = useState<Map<string, Set<string>>>(new Map());
  const [loading, setLoading] = useState(true);
  const [hasMore, setHasMore] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [isTyping, setIsTyping] = useState(false);
  
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const typingTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const stompClientRef = useRef<any>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);
  const sentMessageIdsRef = useRef<Set<string>>(new Set());

  // Load initial messages (load from the end to show recent messages)
  useEffect(() => {
    const loadMessages = async () => {
      try {
        const pageSize = 50;
        // First, get the first page to know total messages
        const { data } = await api.get(`/chat/messages/paginated`, {
          params: { roomId: eventId, page: 0, size: pageSize },
        });
        
        const totalMessages = data.totalMessages || 0;
        const totalPages = data.totalPages || 1;
        
        // Load the last page (most recent messages)
        const lastPage = Math.max(0, totalPages - 1);
        
        // If we're not on the last page, load it
        if (lastPage > 0) {
          const { data: lastPageData } = await api.get(`/chat/messages/paginated`, {
            params: { roomId: eventId, page: lastPage, size: pageSize },
          });
          setMessages(lastPageData.messages || []);
          setHasMore(lastPage > 0);
          setCurrentPage(lastPage);
        } else {
          // Already on first/last page
          setMessages(data.messages || []);
          setHasMore(false);
          setCurrentPage(0);
        }
        
        // Scroll to bottom after loading
        setTimeout(() => scrollToBottom(), 100);
      } catch (error) {
        console.error("Failed to load messages", error);
      } finally {
        setLoading(false);
      }
    };

    if (eventId) {
      loadMessages();
    }
  }, [eventId]);

  // Load older messages (pagination - load previous page)
  const loadMoreMessages = async () => {
    if (!hasMore || loading || currentPage === 0) return;
    
    try {
      setLoading(true);
      const previousPage = currentPage - 1;
      const { data } = await api.get(`/chat/messages/paginated`, {
        params: { roomId: eventId, page: previousPage, size: 50 },
      });
      
      // Save current scroll position
      const container = messagesContainerRef.current;
      const scrollHeightBefore = container?.scrollHeight || 0;
      
      // Prepend older messages
      setMessages((prev) => [...(data.messages || []), ...prev]);
      setHasMore(previousPage > 0);
      setCurrentPage(previousPage);
      
      // Restore scroll position after prepending
      setTimeout(() => {
        if (container) {
          const scrollHeightAfter = container.scrollHeight;
          const scrollDiff = scrollHeightAfter - scrollHeightBefore;
          container.scrollTop = scrollDiff;
        }
      }, 0);
    } catch (error) {
      console.error("Failed to load more messages", error);
    } finally {
      setLoading(false);
    }
  };

  // Initialize WebSocket connection
  useEffect(() => {
    if (!user || !eventId) return;

    // Clean up any existing connection first
    if (stompClientRef.current) {
      stompClientRef.current.deactivate();
      stompClientRef.current = null;
    }

    // Dynamically import SockJS and STOMP
    const initWebSocket = async () => {
      try {
        const SockJS = (await import("sockjs-client")).default;
        const { Client } = await import("@stomp/stompjs");

        const token = localStorage.getItem("token");
        if (!token) return;

        // Create SockJS connection with JWT token
        const socket = new SockJS(
          `http://localhost:8086/ws/chat?token=${encodeURIComponent(token)}&roomId=${eventId}`
        );

        const stompClient = new Client({
          webSocketFactory: () => socket as any,
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          onConnect: () => {
            console.log("WebSocket connected");

            // Subscribe to chat messages
            const chatSubscription = stompClient.subscribe(`/topic/chat/${eventId}`, (message) => {
              const chatMessage: ChatMessage = JSON.parse(message.body);
              // Prevent duplicate messages by checking if message ID already exists
              setMessages((prev) => {
                const exists = prev.some((m) => m.id === chatMessage.id && m.id !== "");
                if (exists) {
                  return prev;
                }
                // Append new message to the end (newest messages at bottom)
                return [...prev, chatMessage];
              });
              // Auto-scroll to bottom when new message arrives
              setTimeout(() => scrollToBottom(), 50);
            });

            // Subscribe to typing indicators
            const typingSubscription = stompClient.subscribe(`/topic/typing/${eventId}`, (message) => {
              const indicator: TypingIndicator = JSON.parse(message.body);
              if (indicator.userId !== user.id?.toString()) {
                if (indicator.isTyping) {
                  setTypingUsers((prev) => new Set([...prev, indicator.userId]));
                  setTimeout(() => {
                    setTypingUsers((prev) => {
                      const next = new Set(prev);
                      next.delete(indicator.userId);
                      return next;
                    });
                  }, 3000);
                } else {
                  setTypingUsers((prev) => {
                    const next = new Set(prev);
                    next.delete(indicator.userId);
                    return next;
                  });
                }
              }
            });

            // Subscribe to read receipts
            const readSubscription = stompClient.subscribe(`/topic/read/${eventId}`, (message) => {
              const receipt: ReadReceipt = JSON.parse(message.body);
              setReadReceipts((prev) => {
                const next = new Map(prev);
                const readers = next.get(receipt.messageId) || new Set();
                readers.add(receipt.userId);
                next.set(receipt.messageId, readers);
                return next;
              });
            });

            // Store subscriptions for cleanup
            (stompClient as any).subscriptions = {
              chat: chatSubscription,
              typing: typingSubscription,
              read: readSubscription,
            };

            // Load online users count
            loadOnlineUsers();
          },
          onStompError: (frame) => {
            console.error("STOMP error:", frame);
          },
        });

        stompClient.activate();
        stompClientRef.current = stompClient;
      } catch (error) {
        console.error("Failed to initialize WebSocket", error);
      }
    };

    initWebSocket();

    return () => {
      if (stompClientRef.current) {
        // Unsubscribe from all topics
        const subscriptions = (stompClientRef.current as any).subscriptions;
        if (subscriptions) {
          subscriptions.chat?.unsubscribe();
          subscriptions.typing?.unsubscribe();
          subscriptions.read?.unsubscribe();
        }
        stompClientRef.current.deactivate();
        stompClientRef.current = null;
      }
    };
  }, [user, eventId]);

  const loadOnlineUsers = async () => {
    try {
      const { data } = await api.get(`/presence`, {
        params: { roomId: eventId },
      });
      setOnlineUsers(Array.isArray(data) ? data.length : 0);
    } catch (error) {
      console.error("Failed to load online users", error);
    }
  };

  const sendMessage = async () => {
    if (!newMessage.trim() || !user) return;

    const messageContent = newMessage.trim();
    setNewMessage("");
    setIsTyping(false);
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    const message: ChatMessage = {
      id: "", // Server will generate ID
      roomId: eventId,
      senderId: user.id.toString(),
      content: messageContent,
      timestamp: Date.now(),
    };

    if (stompClientRef.current && stompClientRef.current.connected) {
      // Send via WebSocket - don't add to local state, wait for server broadcast
      stompClientRef.current.publish({
        destination: `/app/chat.send`,
        body: JSON.stringify(message),
      });
    } else {
      // Fallback to REST API - fetch updated messages after sending
      try {
        await api.post("/chat/send", message);
        // Reload messages to get the server-generated ID
        const { data } = await api.get(`/chat/messages/paginated`, {
          params: { roomId: eventId, page: 0, size: 50 },
        });
        setMessages(data.messages || []);
      } catch (error) {
        console.error("Failed to send message", error);
      }
    }
  };

  const handleTyping = useCallback(() => {
    if (!stompClientRef.current?.connected || !user) return;

    if (!isTyping) {
      setIsTyping(true);
      stompClientRef.current.publish({
        destination: `/app/chat.typing`,
        body: JSON.stringify({
          userId: user.id.toString(),
          roomId: eventId,
          isTyping: true,
          timestamp: Date.now(),
        }),
      });
    }

    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    typingTimeoutRef.current = setTimeout(() => {
      setIsTyping(false);
      if (stompClientRef.current?.connected) {
        stompClientRef.current.publish({
          destination: `/app/chat.typing`,
          body: JSON.stringify({
            userId: user.id.toString(),
            roomId: eventId,
            isTyping: false,
            timestamp: Date.now(),
          }),
        });
      }
    }, 1000);
  }, [isTyping, user, eventId]);

  const markAsRead = (messageId: string) => {
    if (!stompClientRef.current?.connected || !user) return;

    stompClientRef.current.publish({
      destination: `/app/chat.read`,
      body: JSON.stringify({
        messageId,
        userId: user.id.toString(),
        roomId: eventId,
        readAt: Date.now(),
      }),
    });
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  // Auto-scroll to bottom when messages change, but only if user is near bottom
  useEffect(() => {
    const container = messagesContainerRef.current;
    if (!container) return;
    
    // Check if user is near the bottom (within 100px)
    const isNearBottom = 
      container.scrollHeight - container.scrollTop - container.clientHeight < 100;
    
    // Only auto-scroll if user is near bottom (to avoid interrupting scroll-up)
    if (isNearBottom) {
      scrollToBottom();
    }
  }, [messages]);

  // Mark messages as read when they come into view
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          const target = entry.target as HTMLElement;
          const messageId = target.dataset?.messageId;

          if (entry.isIntersecting && messageId) {
            markAsRead(messageId);
          }
        });
      },
      { threshold: 0.5 }
    );

    const messageElements =
      messagesContainerRef.current?.querySelectorAll<HTMLElement>("[data-message-id]");
    messageElements?.forEach((el) => observer.observe(el));

    return () => observer.disconnect();
  }, [messages]);

  if (!user) {
    return (
      <div className="glass rounded-3xl p-6 border border-white/15 shadow-smooth">
        <p className="text-muted text-center">Please log in to join the chat</p>
      </div>
    );
  }

  return (
    <div className="glass rounded-3xl border border-white/15 shadow-smooth flex flex-col h-[600px]">
      {/* Header */}
      <div className="p-4 border-b border-white/10 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <h3 className="font-semibold text-foreground">Event Chat</h3>
          <div className="flex items-center gap-1 text-sm text-muted">
            <Users className="w-4 h-4" />
            <span>{onlineUsers} online</span>
          </div>
        </div>
      </div>

      {/* Messages */}
      <div
        ref={messagesContainerRef}
        className="flex-1 overflow-y-auto p-4 space-y-3 flex flex-col"
      >
        {/* Load older messages button at the top */}
        {hasMore && (
          <button
            onClick={loadMoreMessages}
            disabled={loading}
            className="w-full py-2 text-sm text-muted hover:text-foreground flex items-center justify-center gap-2 sticky top-0 bg-black/20 backdrop-blur-sm rounded-lg z-10"
          >
            <ChevronUp className="w-4 h-4" />
            {loading ? "Loading..." : "Load older messages"}
          </button>
        )}

        {/* Messages list - oldest at top, newest at bottom */}
        <div className="flex-1 flex flex-col">
          {messages.map((message, index) => {
          const isOwn = message.senderId === user.id?.toString();
          const readers = readReceipts.get(message.id) || new Set();
          const isRead = readers.size > 0;
          
          // Use a unique key combining ID, timestamp, and index to prevent duplicates
          const uniqueKey = message.id || `temp-${message.timestamp}-${index}`;

          return (
            <div
              key={uniqueKey}
              data-message-id={message.id || uniqueKey}
              className={`flex ${isOwn ? "justify-end" : "justify-start"} my-0.5`}
            >
              <div
                className={`max-w-[70%] rounded-2xl px-4 py-2 ${
                  isOwn
                    ? "bg-gradient-to-r from-[#c27a48] to-[#8a5a44] text-white"
                    : "bg-white/10 text-foreground"
                }`}
              >
                {!isOwn && (
                  <div className="text-xs text-muted mb-1">
                    {message.senderEmail 
                      ? extractAndCapitalizeName(message.senderEmail)
                      : `User ${message.senderId}`}
                  </div>
                )}
                <p className="text-sm">{message.content}</p>
                <div className="flex items-center justify-end gap-1 mt-1">
                  <span className="text-xs opacity-70">
                    {new Date(message.timestamp).toLocaleTimeString([], {
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </span>
                  {isOwn && isRead && (
                    <span className="text-xs opacity-70">✓✓</span>
                  )}
                </div>
              </div>
            </div>
          );
        })}

          {typingUsers.size > 0 && (
            <div className="text-sm text-muted italic py-2">
              {Array.from(typingUsers).join(", ")} {typingUsers.size === 1 ? "is" : "are"} typing...
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>
      </div>

      {/* Input */}
      <div className="p-4 border-t border-white/10">
        <div className="flex gap-2">
          <input
            type="text"
            value={newMessage}
            onChange={(e) => {
              setNewMessage(e.target.value);
              handleTyping();
            }}
            onKeyPress={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
              }
            }}
            placeholder="Type a message..."
            className="flex-1 px-4 py-2 rounded-full bg-white/5 border border-white/10 text-foreground placeholder:text-muted focus:outline-none focus:ring-2 focus:ring-[#c27a48]"
          />
          <button
            onClick={sendMessage}
            disabled={!newMessage.trim()}
            className="px-6 py-2 rounded-full bg-gradient-to-r from-[#c27a48] to-[#8a5a44] text-white font-semibold hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-opacity"
          >
            <Send className="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>
  );
}
