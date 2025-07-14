import { Component, OnInit } from '@angular/core';
import { ContractService } from '../../services/contract.service';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-contract-list',
  templateUrl: './contract-list.component.html',
  styleUrls: ['./contract-list.component.css']
})
export class ContractListComponent implements OnInit {
  contracts: any[] = [];
  loading = false;
  error = false;
  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  filterForm: FormGroup;

  // Active filters state
  activeFilters: any = {};
  
  // Autocomplete state
  contractingPartySuggestions: string[] = [];
  showSuggestions: boolean = false;
  
  // Maps for type, status, and source codes
  typeCodeMap: { [key: string]: string } = {
    '1': 'Obras',
    '2': 'Servicios',
    '3': 'Suministros',
    '4': 'Concesión de obras',
    '5': 'Concesión de servicios',
    '6': 'Administrativo especial',
    '7': 'Privado',
    '8': 'Patrimonial'
  };

  statusCodeMap: { [key: string]: string } = {
    'PUB': 'Publicado',
    'ADJ': 'Adjudicado',
    'RES': 'Resuelto',
    'CAN': 'Cancelado',
    'DES': 'Desierto'
  };

  sourceCodeMap: { [key: string]: string } = {
    'perfiles': 'Perfiles de Contratante',
    'agregadas': 'Plataformas Agregadas',
    'unknown': 'Origen Desconocido'
  };

  constructor(
    private contractService: ContractService,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      title: [''],
      contractingParty: [''],
      source: [''],
      dateFrom: [''],
      dateTo: ['']
    });
  }

  ngOnInit(): void {
    this.loadContracts();
  }

  loadContracts(): void {
    this.loading = true;
    this.error = false;

    this.contractService.getAllContracts(this.page, this.size).subscribe({
      next: (data) => {
        this.contracts = data.content;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading contracts', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.page = 0; // Reset to first page when applying filters
    this.activeFilters = { ...this.filterForm.value };
    this.loadFilteredContracts();
  }

  loadFilteredContracts(): void {
    this.loading = true;
    this.error = false;
    const filters = this.activeFilters;

    // Check which filter to apply
    if (filters.title) {
      this.contractService.searchContractsByTitle(filters.title, this.page, this.size).subscribe({
        next: (data) => {
          this.handleSearchResponse(data);
        },
        error: (err) => {
          console.error('Error searching contracts by title', err);
          this.error = true;
          this.loading = false;
        }
      });
    } else if (filters.contractingParty) {
      this.contractService.searchContractsByContractingPartyName(filters.contractingParty, this.page, this.size).subscribe({
        next: (data) => {
          this.handleSearchResponse(data);
        },
        error: (err) => {
          console.error('Error searching contracts by contracting party', err);
          this.error = true;
          this.loading = false;
        }
      });
    } else if (filters.source) {
      this.contractService.searchContractsBySource(filters.source, this.page, this.size).subscribe({
        next: (data) => {
          this.handleSearchResponse(data);
        },
        error: (err) => {
          console.error('Error searching contracts by source', err);
          this.error = true;
          this.loading = false;
        }
      });
    } else {
      // If no filters, load all contracts
      this.loadContracts();
    }
  }

  private handleSearchResponse(data: any): void {
    this.contracts = data.content || [];
    this.totalElements = data.totalElements || 0;
    this.totalPages = data.totalPages || 0;
    this.loading = false;
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.activeFilters = {};
    this.page = 0;
    this.loadContracts();
  }

  nextPage(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadCurrentData();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadCurrentData();
    }
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.page = page;
      this.loadCurrentData();
    }
  }

  goToFirstPage(): void {
    this.page = 0;
    this.loadCurrentData();
  }

  goToLastPage(): void {
    this.page = this.totalPages - 1;
    this.loadCurrentData();
  }

  private loadCurrentData(): void {
    if (Object.keys(this.activeFilters).some(key => this.activeFilters[key])) {
      this.loadFilteredContracts();
    } else {
      this.loadContracts();
    }
  }

  getPageNumbers(): number[] {
    const maxVisible = 5;
    const pages: number[] = [];
    
    if (this.totalPages <= maxVisible) {
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      let start = Math.max(1, this.page + 1 - Math.floor(maxVisible / 2));
      let end = Math.min(this.totalPages, start + maxVisible - 1);
      
      if (end - start < maxVisible - 1) {
        start = Math.max(1, end - maxVisible + 1);
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
    }
    
    return pages;
  }

  onContractingPartyInput(event: any): void {
    const query = event.target.value;
    
    if (query.length >= 3) {
      this.contractService.getContractingPartyAutocomplete(query).subscribe({
        next: (suggestions) => {
          this.contractingPartySuggestions = suggestions;
          this.showSuggestions = true;
        },
        error: (err) => {
          console.error('Error fetching autocomplete suggestions', err);
          this.contractingPartySuggestions = [];
          this.showSuggestions = false;
        }
      });
    } else {
      this.contractingPartySuggestions = [];
      this.showSuggestions = false;
    }
  }

  selectSuggestion(suggestion: string): void {
    this.filterForm.patchValue({ contractingParty: suggestion });
    this.contractingPartySuggestions = [];
    this.showSuggestions = false;
  }

  hideSuggestions(): void {
    // Delay hiding to allow click on suggestion
    setTimeout(() => {
      this.showSuggestions = false;
    }, 200);
  }

  /**
   * Get a human-readable label for a type code
   */
  getTypeLabel(typeCode: string): string {
    return this.typeCodeMap[typeCode] || `Tipo ${typeCode}`;
  }

  /**
   * Get a human-readable label for a status code
   */
  getStatusLabel(statusCode: string): string {
    return this.statusCodeMap[statusCode] || `Estado ${statusCode}`;
  }

  /**
   * Get a human-readable label for a source code
   */
  getSourceLabel(sourceCode: string): string {
    return this.sourceCodeMap[sourceCode] || `Origen ${sourceCode}`;
  }

  /**
   * Format currency
   */
  formatCurrency(amount: number): string {
    if (amount === null || amount === undefined) {
      return 'N/A';
    }
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(amount);
  }

  /**
   * Export to CSV
   */
  exportToCsv(): void {
    // Simple CSV export
    const headers = ['Título', 'Organismo', 'Fecha', 'Importe', 'Estado', 'Origen'];
    const csvData = this.contracts.map(contract => [
      contract.title || '',
      contract.contractingPartyName || '',
      contract.updatedAt ? new Date(contract.updatedAt).toLocaleDateString() : '',
      contract.estimatedAmount || '',
      this.getStatusLabel(contract.status || ''),
      this.getSourceLabel(contract.source || '')
    ]);

    // Add headers
    csvData.unshift(headers);

    // Convert to CSV string
    const csvString = csvData.map(row => row.map(cell => `"${cell}"`).join(',')).join('\n');

    // Create download link
    const blob = new Blob([csvString], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', 'contratos.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}