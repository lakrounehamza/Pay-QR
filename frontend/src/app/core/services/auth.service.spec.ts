import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest';

import { AuthService } from './auth.service';
import { TokenService } from './token.service';
import { UserService } from './user.service';
import { Router } from '@angular/router';
import { LoginResponse, UserResponse } from '../models/auth.models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const tokenServiceMock = {
    save: vi.fn(),
    saveAccountId: vi.fn(),
    clear: vi.fn(),
  };

  const userServiceMock = {
    getMyAccount: vi.fn(() => of({ id: 'acc-1' })),
  };

  const routerMock = {
    navigate: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: TokenService, useValue: tokenServiceMock },
        { provide: UserService, useValue: userServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should login, save token/user and accountId', () => {
    const user: UserResponse = {
      id: 'u1',
      email: 'user@mail.com',
      nom: 'Nom',
      prenom: 'Prenom',
      telephone: '0612345678',
      role: 'USER',
      status: 'ACTIVE',
      createdAt: '2026-01-01',
      updatedAt: '2026-01-01',
    };

    const payload: LoginResponse = {
      user,
      token: 'jwt-token',
    };

    service.login({ email: 'user@mail.com', password: 'secret' }).subscribe((res) => {
      expect(res.token).toBe('jwt-token');
    });

    const req = httpMock.expectOne('/api/auth/signin');
    expect(req.request.method).toBe('POST');
    req.flush(payload);

    expect(tokenServiceMock.save).toHaveBeenCalledWith('jwt-token', user);
    expect(userServiceMock.getMyAccount).toHaveBeenCalled();
    expect(tokenServiceMock.saveAccountId).toHaveBeenCalledWith('acc-1');
  });

  it('should logout, clear token storage and redirect', () => {
    service.logout().subscribe();

    const req = httpMock.expectOne('/api/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush({ message: 'ok' });

    expect(tokenServiceMock.clear).toHaveBeenCalled();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
