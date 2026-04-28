export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'USER' | 'ORGANIZER' | 'ADMIN';
  createdAt?: string;
}
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: User;
}
export interface LoginRequest { email: string; password: string; }
export interface RegisterRequest {
  email: string; firstName: string; lastName: string;
  password: string; role?: string;
}