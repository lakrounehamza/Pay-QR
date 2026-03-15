import { createFeatureSelector, createSelector } from '@ngrx/store';
import { UserState } from './user.state';

export const selectUserState = createFeatureSelector<UserState>('user');

export const selectAccount = createSelector(
  selectUserState,
  (state: UserState) => state.account
);

export const selectOperations = createSelector(
  selectUserState,
  (state: UserState) => state.operations
);

export const selectBalance = createSelector(
  selectAccount,
  (account) => account?.solde ?? 0
);

export const selectRecentOperations = createSelector(
  selectOperations,
  (operations) =>
    [...operations].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
);

export const selectLoadingAccount = createSelector(
  selectUserState,
  (state: UserState) => state.loadingAccount
);

export const selectLoadingOperations = createSelector(
  selectUserState,
  (state: UserState) => state.loadingOperations
);

export const selectProcessingTransaction = createSelector(
  selectUserState,
  (state: UserState) => state.processingTransaction
);

export const selectErrorAccount = createSelector(
  selectUserState,
  (state: UserState) => state.errorAccount
);

export const selectErrorOperations = createSelector(
  selectUserState,
  (state: UserState) => state.errorOperations
);

export const selectErrorTransaction = createSelector(
  selectUserState,
  (state: UserState) => state.errorTransaction
);

export const selectSuccessMessage = createSelector(
  selectUserState,
  (state: UserState) => state.successMessage
);
