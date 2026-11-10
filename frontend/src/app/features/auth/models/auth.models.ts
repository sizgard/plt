export interface LoginRequest{
  email: string;
  password: string;
}

export interface RegisterRequest{
  name: string;
  email: string;
  password: string;
}

export interface AuthResponse{
  id: number;
  email: string;
  role: string;
}
