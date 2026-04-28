export interface Notification {
  id: string; type: string; title: string;
  message: string; read: boolean; createdAt: string;
}
export interface ChatMessage {
  id: string; eventId: string; userId: string;
  userEmail: string; content: string; createdAt: string;
}