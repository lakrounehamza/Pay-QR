import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import * as UserActions from './user.actions';
import { UserService } from '../../core/services/user.service';
import { TokenService } from '../../core/services/token.service';

@Injectable()
export class UserEffects {
  private actions$ = inject(Actions);
  private userService = inject(UserService);
  private tokenService = inject(TokenService);
  loadAccount$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.loadAccount),
      switchMap(({ accountId }) => {
        const loadAccount$ = accountId
          ? this.userService.getAccount(accountId)
          : this.userService.getMyAccount();

        return loadAccount$.pipe(
          tap((account) => {
            if (!accountId) {
              this.tokenService.saveAccountId(account.id);
            }
          }),
          map((account) => UserActions.loadAccountSuccess({ account })),
          catchError((error) => {
            if (error?.status === 404) {
              return of(UserActions.createAccount({ userId: this.tokenService.user()?.id ?? '' }));
            }
            return of(
              UserActions.loadAccountFailure({
                error: error?.error?.message ?? 'Failed to load account',
              })
            );
          })
        );
      })
    )
  );

  createAccount$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.createAccount),
      switchMap(({ userId }) =>
        this.userService.createAccount(userId).pipe(
          tap((account) => this.tokenService.saveAccountId(account.id)),
          map((account) => UserActions.createAccountSuccess({ account })),
          catchError((error) =>
            of(
              UserActions.createAccountFailure({
                error: error?.error?.message ?? 'Failed to create account',
              })
            )
          )
        )
      )
    )
  );

  loadAccountSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.loadAccountSuccess),
      map(({ account }) =>
        UserActions.loadOperations({ accountId: account.id })
      )
    )
  );

  createAccountSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.createAccountSuccess),
      map(({ account }) =>
        UserActions.loadOperations({ accountId: account.id })
      )
    )
  );

  loadOperations$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.loadOperations),
      switchMap(({ accountId }) =>
        this.userService.getOperations(accountId).pipe(
          map((operations) => UserActions.loadOperationsSuccess({ operations })),
          catchError((error) =>
            of(
              UserActions.loadOperationsFailure({
                error: error?.error?.message ?? 'Failed to load operations',
              })
            )
          )
        )
      )
    )
  );

  withdraw$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.withdraw),
      switchMap(({ accountId, amount }) =>
        this.userService.createWithdrawal(accountId, amount).pipe(
          switchMap(() => {
            return of(
              UserActions.withdrawSuccess({ message: `Withdrawal of ${amount} MAD submitted` }),
              UserActions.loadAccount({ accountId })
            );
          }),
          catchError((error) =>
            of(
              UserActions.withdrawFailure({
                error: error?.error?.message ?? 'Withdrawal failed',
              })
            )
          )
        )
      )
    )
  );
}
