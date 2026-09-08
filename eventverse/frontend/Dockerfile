# Frontend (user-facing Next.js)
FROM node:26-alpine AS builder
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM node:26-alpine
WORKDIR /app

ENV NODE_ENV=production

COPY --from=builder /app .

EXPOSE 3000

CMD ["npm","run","start"]
