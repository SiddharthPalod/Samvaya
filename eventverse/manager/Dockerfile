# Manager (admin Next.js)
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

# Next.js listens on 3000 inside the container
EXPOSE 3000

CMD ["npm","run","start"]
