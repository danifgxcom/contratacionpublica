import { Component, OnInit } from '@angular/core';
import { ContractService } from '../../services/contract.service';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Chart, ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { Router } from '@angular/router';

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.css']
})
export class StatisticsComponent implements OnInit {
  statistics: any = {};
  loading = true;
  error = false;
  filterForm: FormGroup;
  years: number[] = [];
  regions: {[key: string]: string} = {};

  // Maps for type, status, and source codes
  typeCodeMap: { [key: string]: string } = {
    '1': 'Obras',
    '2': 'Servicios',
    '3': 'Suministros',
    '4': 'Concesión de obras',
    '5': 'Concesión de servicios',
    '6': 'Administrativo especial',
    '7': 'Privado',
    '8': 'Patrimonial',
    '01': 'Obras',
    '02': 'Servicios',
    '03': 'Suministros', 
    '04': 'Concesión de obras',
    '05': 'Concesión de servicios',
    '06': 'Administrativo especial',
    '07': 'Privado',
    '08': 'Patrimonial',
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

  // Chart configurations
  public typeChartData: ChartData = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: [
          '#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796', '#5a5c69', '#f8f9fc'
        ]
      }
    ]
  };

  public statusChartData: ChartData = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: [
          '#1cc88a', '#4e73df', '#f6c23e', '#e74a3b', '#858796'
        ]
      }
    ]
  };

  public sourceChartData: ChartData = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: [
          '#4e73df', '#1cc88a', '#858796'
        ]
      }
    ]
  };

  public monthlyTrendsChartData: ChartData = {
    labels: [],
    datasets: [
      {
        label: 'Número de Contratos',
        data: [],
        borderColor: '#4e73df',
        backgroundColor: 'rgba(78, 115, 223, 0.1)',
        yAxisID: 'y'
      },
      {
        label: 'Valor Total (M€)',
        data: [],
        borderColor: '#1cc88a',
        backgroundColor: 'rgba(28, 200, 138, 0.1)',
        yAxisID: 'y1'
      }
    ]
  };

  public valueDistributionChartData: ChartData = {
    labels: ['Micro (<40k)', 'Pequeño (40k-144k)', 'Mediano (144k-750k)', 'Grande (>750k)'],
    datasets: [
      {
        data: [],
        backgroundColor: [
          '#4e73df', '#1cc88a', '#36b9cc', '#f6c23e'
        ]
      }
    ]
  };

  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: 'top',
      }
    }
  };

  public compactPieOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false
      }
    }
  };

  public doughnutChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
      }
    }
  };

  public lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        type: 'linear',
        display: true,
        position: 'left',
        title: {
          display: true,
          text: 'Número de Contratos'
        }
      },
      y1: {
        type: 'linear',
        display: true,
        position: 'right',
        title: {
          display: true,
          text: 'Valor Total (M€)'
        },
        grid: {
          drawOnChartArea: false,
        },
      },
    },
    plugins: {
      legend: {
        display: true,
        position: 'top'
      }
    }
  };

  public pieChartType: ChartType = 'pie';

  constructor(
    private contractService: ContractService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.filterForm = this.fb.group({
      year: [''],
      region: [''],
      contractType: ['']
    });
  }

  ngOnInit(): void {
    this.loadStatistics();
    this.loadFilterOptions();
  }

  loadFilterOptions(): void {
    // Load years
    this.contractService.getDistinctYears().subscribe({
      next: (years) => {
        this.years = years;
      },
      error: (err) => {
        console.error('Error loading years', err);
      }
    });

    // Load regions
    this.contractService.getDistinctRegions().subscribe({
      next: (regions) => {
        this.regions = regions;
      },
      error: (err) => {
        console.error('Error loading regions', err);
      }
    });
  }

  loadStatistics(): void {
    this.loading = true;
    this.error = false;

    this.contractService.getStatistics().subscribe({
      next: (data) => {
        this.statistics = data;
        this.updateCharts();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading statistics', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  updateCharts(): void {
    this.updateOriginalCharts();
    this.updateMonthlyTrendsChart();
    this.updateValueDistributionChart();
  }

  updateOriginalCharts(): void {
    // Update type chart
    if (this.statistics.countByTypeCode) {
      const typeLabels = Object.keys(this.statistics.countByTypeCode).map(key => this.getTypeLabel(key));
      const typeData = Object.values(this.statistics.countByTypeCode).map((value: any) => Number(value.count || value));

      this.typeChartData.labels = typeLabels;
      this.typeChartData.datasets[0].data = typeData;
    }

    // Update status chart
    if (this.statistics.countByStatus) {
      const statusLabels = Object.keys(this.statistics.countByStatus).map(key => this.getStatusLabel(key));
      const statusData = Object.values(this.statistics.countByStatus).map(value => Number(value));

      this.statusChartData.labels = statusLabels;
      this.statusChartData.datasets[0].data = statusData;
    }

    // Update source chart
    if (this.statistics.countBySource) {
      const sourceLabels = Object.keys(this.statistics.countBySource).map(key => this.getSourceLabel(key));
      const sourceData = Object.values(this.statistics.countBySource).map(value => Number(value));

      this.sourceChartData.labels = sourceLabels;
      this.sourceChartData.datasets[0].data = sourceData;
    }
  }

  updateMonthlyTrendsChart(): void {
    if (this.statistics.monthlyTrends && this.statistics.monthlyTrends.length > 0) {
      const sortedTrends = [...this.statistics.monthlyTrends]
        .sort((a, b) => a.year - b.year || a.month - b.month);

      const labels = sortedTrends.map(trend => `${trend.month}/${trend.year}`);
      const contractCounts = sortedTrends.map(trend => trend.contractCount);
      const amounts = sortedTrends.map(trend => (trend.totalAmount || 0) / 1000000); // Convert to millions

      this.monthlyTrendsChartData.labels = labels;
      this.monthlyTrendsChartData.datasets[0].data = contractCounts;
      this.monthlyTrendsChartData.datasets[1].data = amounts;
    }
  }

  updateValueDistributionChart(): void {
    if (this.statistics.contractValueDistribution) {
      const distribution = this.statistics.contractValueDistribution;
      const data = [
        distribution.microContracts || 0,
        distribution.smallContracts || 0,
        distribution.mediumContracts || 0,
        distribution.largeContracts || 0
      ];

      this.valueDistributionChartData.datasets[0].data = data;
    }
  }

  applyFilters(): void {
    // For now, just reload statistics
    this.loadStatistics();
  }

  resetFilters(): void {
    this.filterForm.reset();
    this.loadStatistics();
  }

  /**
   * Get a human-readable label for a type code
   */
  getTypeLabel(typeCode: unknown): string {
    const code = String(typeCode);
    return this.typeCodeMap[code] || `Tipo ${code}`;
  }

  /**
   * Get a human-readable label for a status code
   */
  getStatusLabel(statusCode: unknown): string {
    const code = String(statusCode);
    return this.statusCodeMap[code] || `Estado ${code}`;
  }

  /**
   * Get a human-readable label for a source code
   */
  getSourceLabel(sourceCode: unknown): string {
    const code = String(sourceCode);
    return this.sourceCodeMap[code] || `Origen ${code}`;
  }

  /**
   * Format currency
   */
  formatCurrency(amount: number): string {
    if (amount === null || amount === undefined) {
      return 'No disponible';
    }
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(amount);
  }

  /**
   * Format numbers with thousand separators
   */
  formatNumber(num: number): string {
    if (num === null || num === undefined) {
      return '0';
    }
    return new Intl.NumberFormat('es-ES').format(num);
  }

  /**
   * Get region participation percentage
   */
  getRegionParticipation(regionAmount: number): number {
    if (!this.statistics.amountAnalysis?.totalAmount || !regionAmount) {
      return 0;
    }
    return Math.round((regionAmount / this.statistics.amountAnalysis.totalAmount) * 100);
  }

  /**
   * Get data quality percentage for organizations
   */
  getDataQualityPercentage(withAmount: number, total: number): number {
    if (!total) return 0;
    return Math.round((withAmount / total) * 100);
  }

  /**
   * Get CSS class for data quality progress bar
   */
  getDataQualityClass(withAmount: number, total: number): string {
    const percentage = this.getDataQualityPercentage(withAmount, total);
    if (percentage >= 80) return 'bg-success';
    if (percentage >= 60) return 'bg-warning';
    return 'bg-danger';
  }

  /**
   * Navigate to contract details page
   */
  goToContract(contractId: string): void {
    this.router.navigate(['/contracts', contractId]);
  }

  /**
   * Navigate to contracts page with contracting party filter
   */
  searchByContractingParty(contractingPartyName: string): void {
    this.router.navigate(['/contracts'], {
      queryParams: { 
        contractingPartyName: contractingPartyName,
        page: 0
      }
    });
  }

  /**
   * Navigate to contracts page with region filter
   */
  searchByRegion(region: string): void {
    this.router.navigate(['/contracts'], {
      queryParams: { 
        countrySubentity: region,
        page: 0
      }
    });
  }

  /**
   * Navigate to contracts page with source filter
   */
  searchBySource(source: string): void {
    this.router.navigate(['/contracts'], {
      queryParams: { 
        source: source,
        page: 0
      }
    });
  }

  /**
   * Navigate to contracts page with amount sorting (descending to show highest first)
   */
  showHighestContracts(): void {
    this.router.navigate(['/contracts'], {
      queryParams: { 
        sort: 'amount,desc',
        page: 0
      }
    });
  }

  /**
   * Navigate to recent contracts (last 30 days)
   */
  showRecentContracts(): void {
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
    
    this.router.navigate(['/contracts'], {
      queryParams: { 
        sort: 'updatedAt,desc',
        page: 0
      }
    });
  }

  /**
   * Navigate to contracts page with type filter
   */
  searchByType(typeCode: string): void {
    this.router.navigate(['/contracts'], {
      queryParams: { 
        typeCode: typeCode,
        page: 0
      }
    });
  }
}