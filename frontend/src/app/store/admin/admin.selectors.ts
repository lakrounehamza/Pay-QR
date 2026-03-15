import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AdminState } from './admin.state';

export const selectAdminState = createFeatureSelector<AdminState>('admin');

export const selectStats = createSelector(
  selectAdminState,
  (state: AdminState) => state.stats
);

export const selectLoadingStats = createSelector(
  selectAdminState,
  (state: AdminState) => state.loadingStats
);

export const selectErrorStats = createSelector(
  selectAdminState,
  (state: AdminState) => state.errorStats
);

export const selectUsers = createSelector(
  selectAdminState,
  (state: AdminState) => state.users
);

export const selectLoadingUsers = createSelector(
  selectAdminState,
  (state: AdminState) => state.loadingUsers
);

export const selectErrorUsers = createSelector(
  selectAdminState,
  (state: AdminState) => state.errorUsers
);

export const selectCurrentUserPage = createSelector(
  selectAdminState,
  (state: AdminState) => state.currentUserPage
);

export const selectTotalUserPages = createSelector(
  selectAdminState,
  (state: AdminState) => state.totalUserPages
);

export const selectAccounts = createSelector(
  selectAdminState,
  (state: AdminState) => state.accounts
);

export const selectLoadingAccounts = createSelector(
  selectAdminState,
  (state: AdminState) => state.loadingAccounts
);

export const selectErrorAccounts = createSelector(
  selectAdminState,
  (state: AdminState) => state.errorAccounts
);

export const selectCurrentAccountPage = createSelector(
  selectAdminState,
  (state: AdminState) => state.currentAccountPage
);

export const selectTotalAccountPages = createSelector(
  selectAdminState,
  (state: AdminState) => state.totalAccountPages
);

export const selectEnterprises = createSelector(
  selectAdminState,
  (state: AdminState) => state.enterprises
);

export const selectLoadingEnterprises = createSelector(
  selectAdminState,
  (state: AdminState) => state.loadingEnterprises
);

export const selectErrorEnterprises = createSelector(
  selectAdminState,
  (state: AdminState) => state.errorEnterprises
);

export const selectCurrentEnterprisePage = createSelector(
  selectAdminState,
  (state: AdminState) => state.currentEnterprisePage
);

export const selectTotalEnterprisePages = createSelector(
  selectAdminState,
  (state: AdminState) => state.totalEnterprisePages
);
