import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, switchMap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import {
  RegisterRequest,
  LoginRequest,
  LoginResponse,
  UserResponse,
  CloudinaryResponse,
  CinAnalysisResult,
} from '../models/auth.models';
import { TokenService } from './token.service';
import { UserService } from './user.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http     = inject(HttpClient);
  private readonly tokenSvc = inject(TokenService);
  private readonly userSvc  = inject(UserService);
  private readonly router   = inject(Router);
  private readonly api      = environment.apiUrl;

  register(request: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.api}/auth/signup`, request);
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.api}/auth/signin`, request).pipe(
      tap(res => this.tokenSvc.save(res.token, res.user)),
      tap(() => {
        this.userSvc.getMyAccount().subscribe({
          next: acc => this.tokenSvc.saveAccountId(acc.id),
          error: () => {},
        });
      })
    );
  }

  logout(): Observable<unknown> {
    return this.http.post(`${this.api}/auth/logout`, {}).pipe(
      tap(() => {
        this.tokenSvc.clear();
        this.router.navigate(['/auth/login']);
      })
    );
  }

  uploadCinImage(file: File): Observable<CloudinaryResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<CloudinaryResponse>(`${this.api}/cin/upload`, formData);
  }

  analyzeCinImage(imageUrl: string): Observable<CinAnalysisResult> {
    return this.http.post<CinAnalysisResult>(`${this.api}/cin/analyze`, { imageUrl });
  }
}
