import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AdminUser,
  AdminAccount,
  AdminEnterprise,
  AdminStats,
  CreateEnterpriseDto,
  PageResponse,
} from '../models/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin`;

  getStatistics(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.base}/statistics`);
  }

  getUsers(page = 0, size = 10): Observable<PageResponse<AdminUser>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<AdminUser>>(`${this.base}/users`, { params });
  }

  activateUser(id: string): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.base}/users/${id}/activate`, {});
  }

  deactivateUser(id: string): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.base}/users/${id}/deactivate`, {});
  }

  getAccounts(page = 0, size = 10): Observable<PageResponse<AdminAccount>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<AdminAccount>>(`${this.base}/accounts`, { params });
  }

  activateAccount(id: string): Observable<AdminAccount> {
    return this.http.patch<AdminAccount>(`${this.base}/accounts/${id}/activate`, {});
  }

  deactivateAccount(id: string): Observable<AdminAccount> {
    return this.http.patch<AdminAccount>(`${this.base}/accounts/${id}/deactivate`, {});
  }

  getEnterprises(page = 0, size = 10): Observable<PageResponse<AdminEnterprise>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<AdminEnterprise>>(`${this.base}/enterprises`, { params });
  }

  createEnterprise(dto: CreateEnterpriseDto): Observable<AdminEnterprise> {
    return this.http.post<AdminEnterprise>(`${this.base}/enterprises`, dto);
  }

  activateEnterprise(id: string): Observable<AdminEnterprise> {
    return this.http.patch<AdminEnterprise>(`${this.base}/enterprises/${id}/activate`, {});
  }

  deactivateEnterprise(id: string): Observable<AdminEnterprise> {
    return this.http.patch<AdminEnterprise>(`${this.base}/enterprises/${id}/deactivate`, {});
  }
}
