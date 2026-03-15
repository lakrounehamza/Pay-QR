import { UserResponse } from '../../core/models/auth.models';

export interface AuthState {
  isLoggedIn: boolean;
  user: UserResponse | null;
  token: string | null;
  accountId: string | null;
  loading: boolean;
  error: string | null;
}

export const initialAuthState: AuthState = {
  isLoggedIn: false,
  user: null,
  token: null,
  accountId: null,
  loading: false,
  error: null,
};
