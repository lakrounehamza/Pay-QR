import { createReducer, on } from '@ngrx/store';
import * as UserActions from './user.actions';
import { UserState, initialUserState } from './user.state';

export const userReducer = createReducer(
  initialUserState,
  on(UserActions.loadAccount, (state) => ({
    ...state,
    loadingAccount: true,
    errorAccount: null,
  })),
  on(UserActions.loadAccountSuccess, (state, { account }) => ({
    ...state,
    account,
    loadingAccount: false,
    errorAccount: null,
  })),
  on(UserActions.loadAccountFailure, (state, { error }) => ({
    ...state,
    loadingAccount: false,
    errorAccount: error,
  })),
  on(UserActions.createAccount, (state) => ({
    ...state,
    loadingAccount: true,
    errorAccount: null,
  })),
  on(UserActions.createAccountSuccess, (state, { account }) => ({
    ...state,
    account,
    loadingAccount: false,
    errorAccount: null,
  })),
  on(UserActions.createAccountFailure, (state, { error }) => ({
    ...state,
    loadingAccount: false,
    errorAccount: error,
  })),
  on(UserActions.loadOperations, (state) => ({
    ...state,
    loadingOperations: true,
    errorOperations: null,
  })),
  on(UserActions.loadOperationsSuccess, (state, { operations }) => ({
    ...state,
    operations,
    loadingOperations: false,
    errorOperations: null,
  })),
  on(UserActions.loadOperationsFailure, (state, { error }) => ({
    ...state,
    loadingOperations: false,
    errorOperations: error,
  })),
  on(UserActions.deposit, (state) => ({
    ...state,
    processingTransaction: true,
    errorTransaction: null,
  })),
  on(UserActions.depositSuccess, (state, { message }) => ({
    ...state,
    processingTransaction: false,
    successMessage: message,
  })),
  on(UserActions.depositFailure, (state, { error }) => ({
    ...state,
    processingTransaction: false,
    errorTransaction: error,
  })),
  on(UserActions.withdraw, (state) => ({
    ...state,
    processingTransaction: true,
    errorTransaction: null,
  })),
  on(UserActions.withdrawSuccess, (state, { message }) => ({
    ...state,
    processingTransaction: false,
    successMessage: message,
  })),
  on(UserActions.withdrawFailure, (state, { error }) => ({
    ...state,
    processingTransaction: false,
    errorTransaction: error,
  })),
  on(UserActions.clearUserError, (state) => ({
    ...state,
    errorAccount: null,
    errorOperations: null,
    errorTransaction: null,
  })),
  on(UserActions.clearSuccessMessage, (state) => ({
    ...state,
    successMessage: null,
  }))
);
