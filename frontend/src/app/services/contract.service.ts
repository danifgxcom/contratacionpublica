import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ContractService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Get all contracts with pagination and sorting
   */
  getAllContracts(page: number = 0, size: number = 10, sort: string = ''): Observable<any> {
    let url = `${this.apiUrl}/contracts?page=${page}&size=${size}`;
    if (sort) {
      url += `&sort=${sort}`;
    }
    return this.http.get(url);
  }

  /**
   * Get a contract by ID
   */
  getContractById(id: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/contracts/${id}`);
  }

  /**
   * Search contracts by title
   */
  searchContractsByTitle(title: string, page: number = 0, size: number = 10, sort: string = ''): Observable<any> {
    let url = `${this.apiUrl}/contracts/search/title?title=${encodeURIComponent(title)}&page=${page}&size=${size}`;
    if (sort) {
      url += `&sort=${sort}`;
    }
    return this.http.get(url);
  }

  /**
   * Search contracts by contracting party name
   */
  searchContractsByContractingPartyName(name: string, page: number = 0, size: number = 10, sort: string = ''): Observable<any> {
    let url = `${this.apiUrl}/contracts/search/contracting-party?name=${encodeURIComponent(name)}&page=${page}&size=${size}`;
    if (sort) {
      url += `&sort=${sort}`;
    }
    return this.http.get(url);
  }

  /**
   * Search contracts by source (perfiles or agregadas)
   */
  searchContractsBySource(source: string, page: number = 0, size: number = 10, sort: string = ''): Observable<any> {
    let url = `${this.apiUrl}/contracts/search/source?source=${encodeURIComponent(source)}&page=${page}&size=${size}`;
    if (sort) {
      url += `&sort=${sort}`;
    }
    return this.http.get(url);
  }

  /**
   * Get contract statistics
   */
  getStatistics(): Observable<any> {
    return this.http.get(`${this.apiUrl}/contracts/statistics`);
  }

  /**
   * Get distinct years from contract updated dates
   */
  getDistinctYears(): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/contracts/years`);
  }

  /**
   * Get distinct regions (NUTS codes and names)
   */
  getDistinctRegions(): Observable<{[key: string]: string}> {
    return this.http.get<{[key: string]: string}>(`${this.apiUrl}/contracts/regions`);
  }

  /**
   * Get autocomplete suggestions for contracting party names
   */
  getContractingPartyAutocomplete(query: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/contracts/autocomplete/contracting-parties?query=${encodeURIComponent(query)}`);
  }

  /**
   * Get global autocomplete suggestions (titles and contracting parties)
   */
  getGlobalAutocomplete(query: string): Observable<{ text: string; type: string }[]> {
    return this.http.get<{ text: string; type: string }[]>(`${this.apiUrl}/contracts/autocomplete/global?query=${encodeURIComponent(query)}&limit=50`);
  }

  /**
   * Global search in multiple fields
   */
  globalSearch(query: string, page: number = 0, size: number = 10, sort: string = ''): Observable<any> {
    let url = `${this.apiUrl}/contracts/search/global?query=${encodeURIComponent(query)}&page=${page}&size=${size}`;
    if (sort) {
      url += `&sort=${sort}`;
    }
    return this.http.get(url);
  }
}
