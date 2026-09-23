import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse } from '../models/models';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  
  currentUser = signal<LoginResponse | null>(this.getStoredUser());

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        localStorage.setItem('jwt_token', res.token);
        localStorage.setItem('user_info', JSON.stringify(res));
        this.currentUser.set(res);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_info');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  isGestor(): boolean {
    return this.currentUser()?.cargo === 'GESTOR';
  }

  isFiscal(): boolean {
    return this.currentUser()?.cargo === 'FISCAL';
  }

  private getStoredUser(): LoginResponse | null {
    const data = localStorage.getItem('user_info');
    return data ? JSON.parse(data) : null;
  }
}
