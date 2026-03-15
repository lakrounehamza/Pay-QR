import { createAction, props } from '@ngrx/store';
import {
  AdminUser,
  AdminAccount,
  AdminEnterprise,
  AdminStats,
} from '../../core/models/admin.models';

export const loadStats = createAction('[Admin] Load Statistics');

export const loadStatsSuccess = createAction(
  '[Admin] Load Statistics Success',
  props<{ stats: AdminStats }>()
);

export const loadStatsFailure = createAction(
  '[Admin] Load Statistics Failure',
  props<{ error: string }>()
);

export const loadUsers = createAction(
  '[Admin] Load Users',
  props<{ page: number; size: number }>()
);

export const loadUsersSuccess = createAction(
  '[Admin] Load Users Success',
  props<{ users: AdminUser[]; currentPage: number; totalPages: number }>()
);

export const loadUsersFailure = createAction(
  '[Admin] Load Users Failure',
  props<{ error: string }>()
);

export const activateUser = createAction(
  '[Admin] Activate User',
  props<{ id: string }>()
);

export const activateUserSuccess = createAction(
  '[Admin] Activate User Success',
  props<{ user: AdminUser }>()
);

export const deactivateUser = createAction(
  '[Admin] Deactivate User',
  props<{ id: string }>()
);

export const deactivateUserSuccess = createAction(
  '[Admin] Deactivate User Success',
  props<{ user: AdminUser }>()
);

export const loadAccounts = createAction(
  '[Admin] Load Accounts',
  props<{ page: number; size: number }>()
);

export const loadAccountsSuccess = createAction(
  '[Admin] Load Accounts Success',
  props<{ accounts: AdminAccount[]; currentPage: number; totalPages: number }>()
);

export const loadAccountsFailure = createAction(
  '[Admin] Load Accounts Failure',
  props<{ error: string }>()
);

export const activateAccount = createAction(
  '[Admin] Activate Account',
  props<{ id: string }>()
);

export const activateAccountSuccess = createAction(
  '[Admin] Activate Account Success',
  props<{ account: AdminAccount }>()
);

export const deactivateAccount = createAction(
  '[Admin] Deactivate Account',
  props<{ id: string }>()
);

export const deactivateAccountSuccess = createAction(
  '[Admin] Deactivate Account Success',
  props<{ account: AdminAccount }>()
);

export const loadEnterprises = createAction(
  '[Admin] Load Enterprises',
  props<{ page: number; size: number }>()
);

export const loadEnterprisesSuccess = createAction(
  '[Admin] Load Enterprises Success',
  props<{ enterprises: AdminEnterprise[]; currentPage: number; totalPages: number }>()
);

export const loadEnterprisesFailure = createAction(
  '[Admin] Load Enterprises Failure',
  props<{ error: string }>()
);

export const activateEnterprise = createAction(
  '[Admin] Activate Enterprise',
  props<{ id: string }>()
);

export const activateEnterpriseSuccess = createAction(
  '[Admin] Activate Enterprise Success',
  props<{ enterprise: AdminEnterprise }>()
);

export const deactivateEnterprise = createAction(
  '[Admin] Deactivate Enterprise',
  props<{ id: string }>()
);

export const deactivateEnterpriseSuccess = createAction(
  '[Admin] Deactivate Enterprise Success',
  props<{ enterprise: AdminEnterprise }>()
);

export const clearAdminError = createAction('[Admin] Clear Error');
