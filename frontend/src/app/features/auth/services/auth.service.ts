import {inject, Injectable, signal} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AuthResponse, LoginRequest, RegisterRequest} from "../models/auth.models";
import {Observable , tap} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AuthService{
  private readonly http = inject(HttpClient)

  readonly currentUser = signal<AuthResponse | null>(null);

  register(req: RegisterRequest): Observable<AuthResponse>{
    return this.http.post<AuthResponse>('/api/auth/register' , req , {
      withCredentials: true
    }).pipe(
      tap(user => this.currentUser.set(user))
    )
  }

  login(req: LoginRequest): Observable<AuthResponse>{
    return this.http.post<AuthResponse>('/api/auth/login', req, {
      withCredentials: true
    }).pipe(
      tap(user => this.currentUser.set(user))
    )
  };

  logout(): Observable<void>{
    return this.http.post<void>('/api/auth/logout',{}, {
      withCredentials: true
    }).pipe(
      tap(() => this.currentUser.set(null))
    )
  }

  getMe(): Observable<AuthResponse>{
    return this.http.get<AuthResponse>('/api/auth/me', {
      withCredentials: true
    }).pipe(
      tap(user => this.currentUser.set(user))
    )
  }
}
