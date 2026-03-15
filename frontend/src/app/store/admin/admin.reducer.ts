import { createReducer, on } from '@ngrx/store';
import * as AdminActions from './admin.actions';
import { AdminState, initialAdminState } from './admin.state';

export const adminReducer = createReducer(
  initialAdminState,
  on(AdminActions.loadStats, (state) => ({
    ...state,
    loadingStats: true,
    errorStats: null,
  })),
  on(AdminActions.loadStatsSuccess, (state, { stats }) => ({
    ...state,
    stats,
    loadingStats: false,
  })),
  on(AdminActions.loadStatsFailure, (state, { error }) => ({
    ...state,
    loadingStats: false,
    errorStats: error,
  })),
  on(AdminActions.loadUsers, (state) => ({
    ...state,
    loadingUsers: true,
    errorUsers: null,
  })),
  on(AdminActions.loadUsersSuccess, (state, { users, currentPage, totalPages }) => ({
    ...state,
    users,
    currentUserPage: currentPage,
    totalUserPages: totalPages,
    loadingUsers: false,
  })),
  on(AdminActions.loadUsersFailure, (state, { error }) => ({
    ...state,
    loadingUsers: false,
    errorUsers: error,
  })),
  on(AdminActions.activateUserSuccess, (state, { user }) => ({
    ...state,
    users: state.users.map((u) => (u.id === user.id ? user : u)),
  })),
  on(AdminActions.deactivateUserSuccess, (state, { user }) => ({
    ...state,
    users: state.users.map((u) => (u.id === user.id ? user : u)),
  })),
  on(AdminActions.loadAccounts, (state) => ({
    ...state,
    loadingAccounts: true,
    errorAccounts: null,
  })),
  on(AdminActions.loadAccountsSuccess, (state, { accounts, currentPage, totalPages }) => ({
    ...state,
    accounts,
    currentAccountPage: currentPage,
    totalAccountPages: totalPages,
    loadingAccounts: false,
  })),
  on(AdminActions.loadAccountsFailure, (state, { error }) => ({
    ...state,
    loadingAccounts: false,
    errorAccounts: error,
  })),
  on(AdminActions.activateAccountSuccess, (state, { account }) => ({
    ...state,
    accounts: state.accounts.map((a) => (a.id === account.id ? account : a)),
  })),
  on(AdminActions.deactivateAccountSuccess, (state, { account }) => ({
    ...state,
    accounts: state.accounts.map((a) => (a.id === account.id ? account : a)),
  })),
  on(AdminActions.loadEnterprises, (state) => ({
    ...state,
    loadingEnterprises: true,
    errorEnterprises: null,
  })),
  on(AdminActions.loadEnterprisesSuccess, (state, { enterprises, currentPage, totalPages }) => ({
    ...state,
    enterprises,
    currentEnterprisePage: currentPage,
    totalEnterprisePages: totalPages,
    loadingEnterprises: false,
  })),
  on(AdminActions.loadEnterprisesFailure, (state, { error }) => ({
    ...state,
    loadingEnterprises: false,
    errorEnterprises: error,
  })),
  on(AdminActions.activateEnterpriseSuccess, (state, { enterprise }) => ({
    ...state,
    enterprises: state.enterprises.map((e) => (e.id === enterprise.id ? enterprise : e)),
  })),
  on(AdminActions.deactivateEnterpriseSuccess, (state, { enterprise }) => ({
    ...state,
    enterprises: state.enterprises.map((e) => (e.id === enterprise.id ? enterprise : e)),
  })),
  on(AdminActions.clearAdminError, (state) => ({
    ...state,
    errorStats: null,
    errorUsers: null,
    errorAccounts: null,
    errorEnterprises: null,
  }))
);
