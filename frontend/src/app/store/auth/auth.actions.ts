import { createAction, props } from '@ngrx/store';
import { LoginRequest, LoginResponse, RegisterRequest, UserResponse } from '../../core/models/auth.models';

export const login = createAction(
  '[Auth] Login',
  props<{ email: string; password: string }>()
);

export const loginSuccess = createAction(
  '[Auth] Login Success',
  props<{ response: LoginResponse }>()
);

export const loginFailure = createAction(
  '[Auth] Login Failure',
  props<{ error: string }>()
);

export const register = createAction(
  '[Auth] Register',
  props<{ request: RegisterRequest }>()
);

export const registerSuccess = createAction(
  '[Auth] Register Success',
  props<{ user: UserResponse }>()
);

export const registerFailure = createAction(
  '[Auth] Register Failure',
  props<{ error: string }>()
);

export const logout = createAction(
  '[Auth] Logout'
);

export const logoutSuccess = createAction(
  '[Auth] Logout Success'
);

export const loadAccountId = createAction(
  '[Auth] Load Account ID',
  props<{ accountId: string }>()
);

export const clearAuth = createAction(
  '[Auth] Clear Auth'
);
