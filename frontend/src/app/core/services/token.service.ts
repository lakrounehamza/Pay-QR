import { Injectable, signal, computed } from '@angular/core';
import { UserResponse } from '../models/auth.models';

const TOKEN_KEY   = 'payqr_token';
const USER_KEY    = 'payqr_user';
const ACCOUNT_KEY = 'payqr_account_id';

@Injectable({ providedIn: 'root' })
export class TokenService {
  private readonly _token  = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private readonly _user   = signal<UserResponse | null>(
    JSON.parse(localStorage.getItem(USER_KEY) ?? 'null')
  );
  private readonly _accountId = signal<string | null>(localStorage.getItem(ACCOUNT_KEY));

  readonly token      = this._token.asReadonly();
  readonly user       = this._user.asReadonly();
  readonly accountId  = this._accountId.asReadonly();
  readonly isLoggedIn   = computed(() => !!this._token());
  readonly role         = computed(() => this._user()?.role ?? null);
  readonly enterpriseId = computed(() => this._user()?.enterpriseId ?? null);

  save(token: string, user: UserResponse): void {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY,  JSON.stringify(user));
    this._token.set(token);
    this._user.set(user);
  }

  saveAccountId(accountId: string): void {
    localStorage.setItem(ACCOUNT_KEY, accountId);
    this._accountId.set(accountId);
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(ACCOUNT_KEY);
    this._token.set(null);
    this._user.set(null);
    this._accountId.set(null);
  }
}
