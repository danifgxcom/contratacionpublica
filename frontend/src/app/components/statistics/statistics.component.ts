import { Component, OnInit } from '@angular/core';
import { ContractService } from '../../services/contract.service';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Chart, ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

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

  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: 'top',
      }
    }
  };

  public pieChartType: ChartType = 'pie';

  constructor(
    private contractService: ContractService,
    private fb: FormBuilder
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
        console.log('Loaded years:', years);
      },
      error: (err) => {
        console.error('Error loading years', err);
      }
    });

    // Load regions
    this.contractService.getDistinctRegions().subscribe({
      next: (regions) => {
        this.regions = regions;
        console.log('Loaded regions:', regions);
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
    // Update type chart
    if (this.statistics.countByTypeCode) {
      const typeLabels = Object.keys(this.statistics.countByTypeCode).map(key => this.getTypeLabel(key));
      const typeData = Object.values(this.statistics.countByTypeCode).map(value => Number(value));

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

  applyFilters(): void {
    // In a real application, this would filter the statistics based on the form values
    // For now, we'll just reload the statistics
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
}
