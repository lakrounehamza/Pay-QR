import { provideEffects } from '@ngrx/effects';
import { provideStore } from '@ngrx/store';

export * from './auth/auth.state';
export * from './auth/auth.actions';
export * from './auth/auth.reducer';
export * from './auth/auth.selectors';
export * from './auth/auth.effects';

export * from './user/user.state';
export * from './user/user.actions';
export * from './user/user.reducer';
export * from './user/user.selectors';
export * from './user/user.effects';

export * from './admin/admin.state';
export * from './admin/admin.actions';
export * from './admin/admin.reducer';
export * from './admin/admin.selectors';
export * from './admin/admin.effects';
