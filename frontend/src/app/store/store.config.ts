import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { authReducer } from './auth/auth.reducer';
import { userReducer } from './user/user.reducer';
import { adminReducer } from './admin/admin.reducer';
import { AuthEffects } from './auth/auth.effects';
import { UserEffects } from './user/user.effects';
import { AdminEffects } from './admin/admin.effects';

export const storeConfig = [
  provideStore(
    {
      auth: authReducer,
      user: userReducer,
      admin: adminReducer,
    },
    {
      runtimeChecks: {
        strictStateImmutability: true,
        strictActionImmutability: true,
        strictStateSerializability: true,
        strictActionSerializability: true,
        strictActionWithinNgZone: true,
        strictActionTypeUniqueness: true,
      },
    }
  ),
  provideEffects([AuthEffects, UserEffects, AdminEffects]),
  provideStoreDevtools({
    maxAge: 25,
    logOnly: true,
  }),
];
