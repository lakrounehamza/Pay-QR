import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import * as AdminActions from './admin.actions';
import { AdminService } from '../../core/services/admin.service';

@Injectable()
export class AdminEffects {
  private actions$ = inject(Actions);
  private adminService = inject(AdminService);
  loadStats$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AdminActions.loadStats),
      switchMap(() =>
        this.adminService.getStatistics().pipe(
          map((stats) => AdminActions.loadStatsSuccess({ stats })),
          catchError((error) =>
            of(AdminActions.loadStatsFailure({ error: error?.error?.message ?? 'Failed to load statistics' }))
          )
        )
      )
    )
  );

  loadUsers$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AdminActions.loadUsers),
      switchMap(({ page, size }) =>
        this.adminService.getUsers(page, size).pipe(
          map((response) =>
            AdminActions.loadUsersSuccess({
              users: response.content,
              currentPage: response.currentPage,
              totalPages: response.totalPages,
            })
          ),
          catchError((error) =>
            of(AdminActions.loadUsersFailure({ error: error?.error?.message ?? 'Failed to load users' }))
          )
        )
      )
    )
  );

  activateUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AdminActions.activateUser),
      switchMap(({ id }) =>
        this.adminService.activateUser(id).pipe(
          map((user) => AdminActions.activateUserSuccess({ user })),
          catchError((error) =>
            of(AdminActions.loadUsersFailure({ error: error?.error?.message ?? 'Failed to activate user' }))
          )
        )
      )
    )
  );

  deactivateUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AdminActions.deactivateUser),
      switchMap(({ id }) =>
        this.adminService.deactivateUser(id).pipe(
          map((user) => AdminActions.deactivateUserSuccess({ user })),
          catchError((error) =>
            of(AdminActions.loadUsersFailure({ error: error?.error?.message ?? 'Failed to deactivate user' }))
          )
        )
      )
    )
  );

  loadAccounts$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AdminActions.loadAccounts),
      switchMap(({ page, size }) =>
        this.adminService.getAccounts(page, size).pipe(
          map((response) =>
            AdminActions.loadAccountsSuccess({
              accounts: response.content,
              currentPage: response.currentPage,
              totalPages: response.totalPages,
            })
          ),
          catchError((error) =>
            of(AdminActions.loadAccountsFailure({ error: error?.error?.message ?? 'Failed to load accounts' }))
          )
        )
      )
    )
  );

  activateAccount$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AdminActions.activateAccount),
      switchMap(({ id }) =>
        this.adminService.activateAccount(id).pipe(
          map((account) => AdminActions.activateAccountSuccess({ account })),
          catchError((error) =>
            of(AdminActions.loadAccountsFailure({ error: error?.error?.message ?? 'Failed to activate account' }))
          )
        )
      )
    )
  );

  deactivateAccount$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AdminActions.deactivateAccount),
      switchMap(({ id }) =>
        this.adminService.deactivateAccount(id).pipe(
          map((account) => AdminActions.deactivateAccountSuccess({ account })),
          catchError((error) =>
            of(AdminActions.loadAccountsFailure({ error: error?.error?.message ?? 'Failed to deactivate account' }))
          )
        )
      )
    )
  );

  loadEnterprises$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AdminActions.loadEnterprises),
      switchMap(({ page, size }) =>
        this.adminService.getEnterprises(page, size).pipe(
          map((response) =>
            AdminActions.loadEnterprisesSuccess({
              enterprises: response.content,
              currentPage: response.currentPage,
              totalPages: response.totalPages,
            })
          ),
          catchError((error) =>
            of(AdminActions.loadEnterprisesFailure({ error: error?.error?.message ?? 'Failed to load enterprises' }))
          )
        )
      )
    )
  );

  activateEnterprise$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AdminActions.activateEnterprise),
      switchMap(({ id }) =>
        this.adminService.activateEnterprise(id).pipe(
          map((enterprise) => AdminActions.activateEnterpriseSuccess({ enterprise })),
          catchError((error) =>
            of(AdminActions.loadEnterprisesFailure({ error: error?.error?.message ?? 'Failed to activate enterprise' }))
          )
        )
      )
    )
  );

  deactivateEnterprise$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AdminActions.deactivateEnterprise),
      switchMap(({ id }) =>
        this.adminService.deactivateEnterprise(id).pipe(
          map((enterprise) => AdminActions.deactivateEnterpriseSuccess({ enterprise })),
          catchError((error) =>
            of(AdminActions.loadEnterprisesFailure({ error: error?.error?.message ?? 'Failed to deactivate enterprise' }))
          )
        )
      )
    )
  );
}
