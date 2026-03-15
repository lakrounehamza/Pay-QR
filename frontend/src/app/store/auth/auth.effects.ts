import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import * as AuthActions from './auth.actions';
import { AuthService } from '../../core/services/auth.service';
import { TokenService } from '../../core/services/token.service';
import { UserService } from '../../core/services/user.service';

@Injectable()
export class AuthEffects {
  private actions$ = inject(Actions);
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private tokenService = inject(TokenService);
  private router = inject(Router);
  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.login),
      switchMap(({ email, password }) =>
        this.authService.login({ email, password }).pipe(
          tap((response) => {
            this.tokenService.save(response.token, response.user);
          }),
          switchMap((response) =>
            this.userService.getMyAccount().pipe(
              tap((account) => this.tokenService.saveAccountId(account.id)),
              map(() => AuthActions.loginSuccess({ response })),
              catchError(() => of(AuthActions.loginSuccess({ response })))
            )
          ),
          catchError((error) =>
            of(AuthActions.loginFailure({ error: error?.error?.message ?? 'Login failed' }))
          )
        )
      )
    )
  );

  loginSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.loginSuccess),
        tap(({ response }) => {
          const role = response.user.role;
          if (role === 'ADMIN') {
            this.router.navigate(['/admin']);
          } else if (role === 'ENTERPRISE_ADMIN') {
            this.router.navigate(['/enterprise']);
          } else {
            this.router.navigate(['/']);
          }
        })
      ),
    { dispatch: false }
  );

  register$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.register),
      switchMap(({ request }) =>
        this.authService.register(request).pipe(
          map((user) => AuthActions.registerSuccess({ user })),
          catchError((error) =>
            of(AuthActions.registerFailure({ error: error?.error?.message ?? 'Registration failed' }))
          )
        )
      )
    )
  );

  logout$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.logout),
      switchMap(() =>
        this.authService.logout().pipe(
          map(() => {
            this.tokenService.clear();
            return AuthActions.logoutSuccess();
          }),
          catchError(() => {
            this.tokenService.clear();
            return of(AuthActions.logoutSuccess());
          })
        )
      )
    )
  );

  logoutSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.logoutSuccess),
        tap(() => this.router.navigate(['/auth/login']))
      ),
    { dispatch: false }
  );
}
