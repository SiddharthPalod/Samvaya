// components/Navbar.tsx
"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { useTheme } from "@/context/ThemeContext";
import { Calendar, LogOut, Moon, Sun, User as UserIcon, Sparkles, Menu, X, Volume2 } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { useState, useEffect } from "react";

export default function Navbar() {
  const { user, logout } = useAuth();
  const pathname = usePathname();
  const { theme, toggleTheme } = useTheme();
  const isLight = theme === "light";
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const navLinks = [
    { href: "/", label: "Home" },
    { href: "/feed", label: "Feed" },
    { href: "/events", label: "Events" },
    ...(user ? [
      { href: "/my-tickets", label: "My Tickets" },
      { href: "/notifications", label: "Notifications" },
    ] : []),
  ];

  // Close menu when route changes
  useEffect(() => {
    setIsMenuOpen(false);
  }, [pathname]);

  return (
    <>
      {/* Always visible menu button */}
      <motion.button
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3 }}
        onClick={() => setIsMenuOpen(!isMenuOpen)}
        className={`fixed top-4 left-4 z-[60] h-12 w-12 rounded-full flex items-center justify-center shadow-lg backdrop-blur-md transition-all ${
          isLight
            ? "bg-gradient-to-br from-[#f0e4d5] via-[#e8dcc9] to-[#f0e4d5] border border-black/10 text-[#1f140d] hover:scale-110"
            : "bg-white/10 border border-white/20 text-foreground hover:bg-white/15"
        }`}
        aria-label="Toggle menu"
      >
        {isMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
      </motion.button>

      {/* Always visible theme toggle */}
      <motion.button
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3, delay: 0.1 }}
        onClick={toggleTheme}
        className={`fixed top-4 right-4 z-[60] h-12 w-12 rounded-full flex items-center justify-center shadow-lg backdrop-blur-md transition-all ${
          isLight
            ? "bg-gradient-to-br from-[#f0e4d5] via-[#e8dcc9] to-[#f0e4d5] border border-black/10 text-[#1f140d] hover:scale-110"
            : "bg-white/10 border border-white/20 text-foreground hover:bg-white/15"
        }`}
        aria-label="Toggle theme"
      >
        {theme === "dark" ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
      </motion.button>

      {/* Menu dialog - works on all screen sizes */}
      <AnimatePresence>
        {isMenuOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setIsMenuOpen(false)}
              className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50"
            />
            <motion.div
              initial={{ x: "-100%" }}
              animate={{ x: 0 }}
              exit={{ x: "-100%" }}
              transition={{ type: "spring", damping: 25, stiffness: 200 }}
              className={`fixed top-0 left-0 h-full w-80 sm:w-96 z-[55] shadow-2xl ${
                isLight
                  ? "bg-gradient-to-br from-[#f7f1e9] via-[#f0e4d5] to-[#e8dcc9]"
                  : "bg-[rgba(28,16,10,0.98)] border-r border-white/10"
              }`}
            >
              <div className="p-6 pt-24 space-y-6">
                {/* Navigation Links */}
                <div className="space-y-2">
                  <h2 className={`text-xs font-bold uppercase tracking-[0.2em] mb-4 ${
                    isLight ? "text-[#7b6757]" : "text-muted"
                  }`}>
                    Navigation
                  </h2>
                  {navLinks.map((link) => {
                    const isActive = pathname === link.href;
                    return (
                      <Link
                        key={link.href}
                        href={link.href}
                        onClick={() => setIsMenuOpen(false)}
                        className={`block px-4 py-3 text-base font-medium rounded-xl transition-all ${
                          isActive
                            ? isLight
                              ? "text-[#1f140d] bg-gradient-to-br from-[#f0e4d5] to-[#e8dcc9] border border-black/10"
                              : "text-foreground bg-white/10 border border-white/20"
                            : isLight
                              ? "text-[#7b6757] hover:text-[#1f140d] hover:bg-gradient-to-br hover:from-[#f0e4d5]/60 hover:to-[#e8dcc9]/60"
                              : "text-muted hover:text-foreground hover:bg-white/5"
                        }`}
                      >
                        {link.label}
                      </Link>
                    );
                  })}
                </div>

                {/* User section */}
                {user ? (
                  <div className={`pt-4 border-t space-y-3 ${
                    isLight ? "border-black/10" : "border-white/10"
                  }`}>
                    <h2 className={`text-xs font-bold uppercase tracking-[0.2em] mb-4 ${
                      isLight ? "text-[#7b6757]" : "text-muted"
                    }`}>
                      Account
                    </h2>
                    <div className={`flex items-center gap-2 px-4 py-3 rounded-xl ${
                      isLight
                        ? "bg-gradient-to-br from-[#f0e4d5]/80 to-[#e8dcc9]/80"
                        : "bg-white/5"
                    }`}>
                      <UserIcon className={`h-5 w-5 ${isLight ? "text-[#7b6757]" : "text-foreground/80"}`} />
                      <span className={`text-sm truncate ${isLight ? "text-[#1f140d]" : "text-foreground"}`}>
                        {user.email}
                      </span>
                    </div>
                    <button
                      onClick={() => {
                        logout();
                        setIsMenuOpen(false);
                      }}
                      className={`w-full flex items-center justify-center px-4 py-3 rounded-xl text-sm font-semibold transition-all ${
                        isLight
                          ? "text-[#120a06] bg-gradient-to-br from-white to-[#f0e4d5] border border-black/10 hover:scale-[1.02]"
                          : "text-[#120a06] bg-white hover:bg-white/90 hover:scale-[1.02]"
                      }`}
                    >
                      <LogOut className="h-4 w-4 mr-2" />
                      Logout
                    </button>
                  </div>
                ) : (
                  <div className={`pt-4 border-t space-y-3 ${
                    isLight ? "border-black/10" : "border-white/10"
                  }`}>
                    <h2 className={`text-xs font-bold uppercase tracking-[0.2em] mb-4 ${
                      isLight ? "text-[#7b6757]" : "text-muted"
                    }`}>
                      Account
                    </h2>
                    <Link
                      href="/login"
                      onClick={() => setIsMenuOpen(false)}
                      className={`block w-full text-center px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                        isLight
                          ? "text-[#7b6757] hover:text-[#1f140d] hover:bg-gradient-to-br hover:from-[#f0e4d5]/60 hover:to-[#e8dcc9]/60"
                          : "text-muted hover:text-foreground hover:bg-white/5"
                      }`}
                    >
                      Login
                    </Link>
                    <Link
                      href="/register"
                      onClick={() => setIsMenuOpen(false)}
                      className={`block w-full text-center px-4 py-3 rounded-xl text-sm font-semibold transition-transform ${
                        isLight
                          ? "text-[#120a06] bg-gradient-to-br from-white to-[#f0e4d5] border border-black/10 hover:-translate-y-px"
                          : "text-[#120a06] bg-white hover:bg-white/90 hover:-translate-y-px"
                      }`}
                    >
                      Get Started
                    </Link>
                  </div>
                )}
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </>
  );
}

