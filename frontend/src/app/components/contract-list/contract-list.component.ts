import { Component, OnInit } from '@angular/core';
import { ContractService } from '../../services/contract.service';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

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
  
  // Page size options
  pageSizeOptions = [10, 25, 50, 100];
  filterForm: FormGroup;

  // Active filters state
  activeFilters: any = {};
  
  // Autocomplete state
  contractingPartySuggestions: string[] = [];
  showSuggestions: boolean = false;
  
  // Autonomous communities
  autonomousCommunities: string[] = [];
  
  // Sorting state
  sortColumns: { field: string; direction: 'asc' | 'desc' }[] = [];
  
  // Maps for type, status, and source codes
  typeCodeMap: { [key: string]: string } = {
    // Basic types
    '1': 'Obras',
    '2': 'Servicios', 
    '3': 'Suministros',
    '4': 'Concesión de obras',
    '5': 'Concesión de servicios',
    '6': 'Administrativo especial',
    '7': 'Privado',
    '8': 'Patrimonial',
    // Two-digit format
    '01': 'Obras',
    '02': 'Servicios',
    '03': 'Suministros', 
    '04': 'Concesión de obras',
    '05': 'Concesión de servicios',
    '06': 'Administrativo especial',
    '07': 'Privado',
    '08': 'Patrimonial',
    // Extended official types
    '21': 'Obras - Arrendamiento',
    '22': 'Obras - Arrendamiento con opción de compra',
    '31': 'Suministros - Arrendamiento',
    '32': 'Suministros - Arrendamiento con opción de compra',
    '50': 'Gestión de servicios públicos',
    '99': 'Otro o mixto',
    '999': 'Otros',
    'Unknown': 'Tipo desconocido'
  };

  statusCodeMap: { [key: string]: string } = {
    'PUB': 'Publicado',
    'ADJ': 'Adjudicado',
    'RES': 'Resuelto',
    'CAN': 'Cancelado',
    'DES': 'Desierto',
    'PRE': 'Anuncio previo',
    'EV': 'Pendiente de adjudicación',
    'ANUL': 'Anulada'
  };

  sourceCodeMap: { [key: string]: string } = {
    'perfiles': 'Perfiles de Contratante',
    'agregadas': 'Plataformas Agregadas',
    'unknown': 'Origen Desconocido'
  };

  constructor(
    private contractService: ContractService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.filterForm = this.fb.group({
      title: [''],
      contractingParty: [''],
      source: [''],
      countrySubentity: [''],
      dateFrom: [''],
      dateTo: ['']
    });
  }

  ngOnInit(): void {
    // Load autonomous communities
    this.loadAutonomousCommunities();
    
    // Check for global search query parameter
    this.route.queryParams.subscribe(params => {
      if (params['search']) {
        this.performGlobalSearch(params['search']);
      } else {
        this.loadContracts();
      }
    });
  }

  loadAutonomousCommunities(): void {
    this.contractService.getRegions().subscribe({
      next: (regions) => {
        this.autonomousCommunities = Object.values(regions).filter(region => region && region.trim() !== '');
      },
      error: (err) => {
        console.error('Error loading autonomous communities', err);
        this.autonomousCommunities = [];
      }
    });
  }

  loadContracts(): void {
    this.loading = true;
    this.error = false;
    const sortQuery = this.buildSortQuery();

    this.contractService.getAllContracts(this.page, this.size, sortQuery).subscribe({
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
    const sortQuery = this.buildSortQuery();

    // Check which filter to apply
    if (filters.title) {
      this.contractService.searchContractsByTitle(filters.title, this.page, this.size, sortQuery).subscribe({
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
      this.contractService.searchContractsByContractingPartyName(filters.contractingParty, this.page, this.size, sortQuery).subscribe({
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
      this.contractService.searchContractsBySource(filters.source, this.page, this.size, sortQuery).subscribe({
        next: (data) => {
          this.handleSearchResponse(data);
        },
        error: (err) => {
          console.error('Error searching contracts by source', err);
          this.error = true;
          this.loading = false;
        }
      });
    } else if (filters.countrySubentity) {
      this.contractService.searchContractsByCountrySubentity(filters.countrySubentity, this.page, this.size, sortQuery).subscribe({
        next: (data) => {
          this.handleSearchResponse(data);
        },
        error: (err) => {
          console.error('Error searching contracts by country subentity', err);
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
  
  onPageSizeChange(event: any): void {
    const newSize = parseInt(event.target.value, 10);
    this.size = newSize;
    this.page = 0; // Reset to first page
    this.loadCurrentData();
  }

  private loadCurrentData(): void {
    if (Object.keys(this.activeFilters).some(key => this.activeFilters[key])) {
      this.loadFilteredContracts();
    } else {
      this.loadContracts();
    }
  }

  performGlobalSearch(query: string): void {
    this.loading = true;
    this.error = false;
    const sortQuery = this.buildSortQuery();

    this.contractService.globalSearch(query, this.page, this.size, sortQuery).subscribe({
      next: (data) => {
        this.handleSearchResponse(data);
      },
      error: (err) => {
        console.error('Error performing global search', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  private buildSortQuery(): string {
    if (this.sortColumns.length === 0) {
      return '';
    }
    
    return this.sortColumns
      .map(col => `${col.field},${col.direction}`)
      .join('&sort=');
  }

  getPageNumbers(): number[] {
    const maxVisible = 5;
    const pages: number[] = [];
    const currentPage = this.page + 1; // Convert to 1-based
    
    if (this.totalPages <= maxVisible) {
      // Show all pages if total is small
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Always show first page
      pages.push(1);
      
      // Calculate range around current page
      let start = Math.max(2, currentPage - 1);
      let end = Math.min(this.totalPages - 1, currentPage + 1);
      
      // Add separator if needed
      if (start > 2) {
        pages.push(-1); // -1 represents "..."
      }
      
      // Add pages around current
      for (let i = start; i <= end; i++) {
        if (i !== 1 && i !== this.totalPages) {
          pages.push(i);
        }
      }
      
      // Add separator if needed
      if (end < this.totalPages - 1) {
        pages.push(-1); // -1 represents "..."
      }
      
      // Always show last page
      if (this.totalPages > 1) {
        pages.push(this.totalPages);
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

  onColumnClick(field: string): void {
    const existingIndex = this.sortColumns.findIndex(col => col.field === field);
    
    if (existingIndex === -1) {
      // First click: add ascending
      this.sortColumns.push({ field, direction: 'asc' });
    } else {
      const currentDirection = this.sortColumns[existingIndex].direction;
      if (currentDirection === 'asc') {
        // Second click: change to descending
        this.sortColumns[existingIndex].direction = 'desc';
      } else {
        // Third click: remove from sorting
        this.sortColumns.splice(existingIndex, 1);
      }
    }
    
    // Reset to first page when sorting changes
    this.page = 0;
    this.loadCurrentData();
  }

  getSortIcon(field: string): string {
    const sortColumn = this.sortColumns.find(col => col.field === field);
    if (!sortColumn) return '';
    
    return sortColumn.direction === 'asc' ? '↑' : '↓';
  }

  getSortOrder(field: string): number {
    const index = this.sortColumns.findIndex(col => col.field === field);
    return index === -1 ? 0 : index + 1;
  }

  navigateToContract(contractId: string): void {
    this.router.navigate(['/contracts', contractId]);
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
   * Get the best available amount from a contract (same logic as backend)
   */
  getBestAmount(contract: any): number | null {
    // First try the structured fields
    const structuredAmount = contract.totalAmount || contract.taxExclusiveAmount || contract.estimatedAmount;
    
    // If structured amount is 0 or null, try to parse from summary
    if (!structuredAmount || structuredAmount === 0) {
      const parsedAmount = this.parseAmountFromSummary(contract.summary);
      if (parsedAmount && parsedAmount > 0) {
        return parsedAmount;
      }
    }
    
    return structuredAmount || null;
  }

  /**
   * Parse amount from summary field when structured fields are 0
   */
  private parseAmountFromSummary(summary: string): number | null {
    if (!summary) return null;
    
    // Try to find patterns like "Importe: 4824.00 EUR" or "Importe: 4,824.00 EUR"
    const patterns = [
      /Importe:\s*([0-9]+(?:[.,][0-9]+)?)\s*EUR/i,
      /Importe:\s*([0-9]+(?:\.[0-9]{3})*(?:,[0-9]+)?)\s*EUR/i,
      /([0-9]+(?:[.,][0-9]+)?)\s*EUR/i
    ];
    
    for (const pattern of patterns) {
      const match = summary.match(pattern);
      if (match) {
        const amountStr = match[1].replace(/\./g, '').replace(',', '.');
        const amount = parseFloat(amountStr);
        if (!isNaN(amount)) {
          return amount;
        }
      }
    }
    
    return null;
  }

  /**
   * Format currency
   */
  formatCurrency(amount: number | null): string {
    if (amount === null || amount === undefined) {
      return 'Sin importe';
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