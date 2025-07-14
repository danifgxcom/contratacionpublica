import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { ContractService } from '../../services/contract.service';
import { ChartConfiguration, ChartData } from 'chart.js';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  statistics: any = {};
  loading = true;
  error = false;
  lastUpdateDate = new Date();
  searchControl = new FormControl('');
  showAnomalies = false;

  // Chart data
  typeChartData: ChartData = {
    labels: [],
    datasets: []
  };

  statusChartData: ChartData = {
    labels: [],
    datasets: []
  };

  chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: 'top',
      }
    }
  };

  // Maps for type, status, and source codes
  private typeCodeMap: { [key: string]: string } = {
    '1': 'Obras',
    '2': 'Servicios',
    '3': 'Suministros',
    '4': 'Concesión de obras',
    '5': 'Concesión de servicios',
    '6': 'Administrativo especial',
    '7': 'Privado',
    '8': 'Patrimonial'
  };

  private statusCodeMap: { [key: string]: string } = {
    'PUB': 'Publicado',
    'ADJ': 'Adjudicado',
    'RES': 'Resuelto',
    'CAN': 'Cancelado',
    'DES': 'Desierto'
  };

  private sourceCodeMap: { [key: string]: string } = {
    'perfiles': 'Perfiles de Contratante',
    'agregadas': 'Plataformas Agregadas',
    'unknown': 'Origen Desconocido'
  };

  constructor(
    private contractService: ContractService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.loading = true;
    this.error = false;

    this.contractService.getStatistics().subscribe({
      next: (data) => {
        console.log('Statistics data:', data);
        this.statistics = data;

        // Debug countByTypeCode structure
        if (data.countByTypeCode) {
          console.log('countByTypeCode structure:', data.countByTypeCode);
          console.log('Type 1 (Obras):', data.countByTypeCode['1']);
          console.log('Type 2 (Servicios):', data.countByTypeCode['2']);
          console.log('Type 3 (Suministros):', data.countByTypeCode['3']);
        }

        // Debug topOrganizations structure
        if (data.topOrganizations) {
          console.log('topOrganizations structure:', data.topOrganizations);
        }

        this.loading = false;
        this.prepareChartData();
      },
      error: (err) => {
        console.error('Error loading statistics', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  /**
   * Prepare chart data from statistics
   */
  prepareChartData(): void {
    console.log('Preparing chart data...');

    // Prepare type chart data
    if (this.statistics.countByTypeCode) {
      console.log('Preparing type chart data from:', this.statistics.countByTypeCode);

      const labels: string[] = [];
      const data: number[] = [];
      const backgroundColor: string[] = [
        '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#C9CBCF', '#7CFC00'
      ];

      // Process the data to ensure we have values for the chart
      const processedData: { [key: string]: number } = {};

      // First, extract all the counts from the data
      Object.entries(this.statistics.countByTypeCode).forEach(([key, value]: [string, any]) => {
        console.log(`Processing type ${key}:`, value);

        let count = 0;
        // Check if value has count property
        if (value && typeof value === 'object' && 'count' in value) {
          count = value.count;
        } else if (typeof value === 'number') {
          count = value;
        }

        // Store the count with the appropriate label
        const label = this.getTypeLabel(key);

        // If we already have a count for this label, add to it
        if (processedData[label]) {
          processedData[label] += count;
        } else {
          processedData[label] = count;
        }
      });

      console.log('Processed data:', processedData);

      // Convert the processed data to arrays for the chart
      Object.entries(processedData).forEach(([label, count]) => {
        labels.push(label);
        data.push(count);
      });

      console.log('Type chart labels:', labels);
      console.log('Type chart data:', data);

      // Only create the chart if we have data
      if (labels.length > 0 && data.length > 0) {
        this.typeChartData = {
          labels: labels,
          datasets: [{
            data: data,
            backgroundColor: backgroundColor.slice(0, labels.length)
          }]
        };

        console.log('Final typeChartData:', this.typeChartData);
      } else {
        console.warn('No valid data for type chart');
      }
    } else {
      console.warn('No countByTypeCode data available for chart');
    }

    // Prepare status chart data
    if (this.statistics.countByStatus) {
      console.log('Preparing status chart data from:', this.statistics.countByStatus);

      const labels: string[] = [];
      const data: number[] = [];
      const backgroundColor: string[] = [
        '#36A2EB', '#4BC0C0', '#FFCE56', '#FF6384', '#9966FF'
      ];

      Object.entries(this.statistics.countByStatus).forEach(([key, value]: [string, any]) => {
        console.log(`Processing status ${key}:`, value);
        labels.push(this.getStatusLabel(key));
        data.push(value);
      });

      console.log('Status chart labels:', labels);
      console.log('Status chart data:', data);

      this.statusChartData = {
        labels: labels,
        datasets: [{
          label: 'Contratos por Estado',
          data: data,
          backgroundColor: backgroundColor.slice(0, labels.length)
        }]
      };

      console.log('Final statusChartData:', this.statusChartData);
    } else {
      console.warn('No countByStatus data available for chart');
    }
  }

  /**
   * Get contract count by type
   */
  getContractCountByType(typeCode: string): number {
    console.log(`Getting count for type ${typeCode}`);

    if (!this.statistics.countByTypeCode) {
      console.warn('No countByTypeCode data available');
      return 0;
    }

    // Try with the provided code first
    if (this.statistics.countByTypeCode[typeCode]) {
      const typeData = this.statistics.countByTypeCode[typeCode];
      console.log(`Type ${typeCode} data:`, typeData);

      // Check if typeData has count property
      if (typeData && typeof typeData === 'object' && 'count' in typeData) {
        console.log(`Returning count: ${typeData.count}`);
        return typeData.count;
      } else if (typeof typeData === 'number') {
        console.log(`Returning number value: ${typeData}`);
        return typeData;
      }
    }

    // If not found, try with padded code (e.g., '1' -> '01')
    const paddedCode = typeCode.padStart(2, '0');
    console.log(`Trying with padded code: ${paddedCode}`);

    if (this.statistics.countByTypeCode[paddedCode]) {
      const typeData = this.statistics.countByTypeCode[paddedCode];
      console.log(`Type ${paddedCode} data:`, typeData);

      // Check if typeData has count property
      if (typeData && typeof typeData === 'object' && 'count' in typeData) {
        console.log(`Returning count: ${typeData.count}`);
        return typeData.count;
      } else if (typeof typeData === 'number') {
        console.log(`Returning number value: ${typeData}`);
        return typeData;
      }
    }

    // If still not found, log warning and return 0
    console.warn(`No data for type ${typeCode} or ${typeCode.padStart(2, '0')}`);
    return 0;
  }

  /**
   * Get contract count by status
   */
  getContractCountByStatus(statusCode: string): number {
    if (!this.statistics.countByStatus || !this.statistics.countByStatus[statusCode]) {
      return 0;
    }
    return this.statistics.countByStatus[statusCode];
  }

  /**
   * Get contract count by source
   */
  getContractCountBySource(sourceCode: string): number {
    if (!this.statistics.countBySource || !this.statistics.countBySource[sourceCode]) {
      return 0;
    }
    return this.statistics.countBySource[sourceCode];
  }

  /**
   * Get unknown type items for anomalies section
   */
  getUnknownTypeItems(): { [key: string]: number } {
    const result: { [key: string]: number } = {};

    if (this.statistics.countByTypeCode) {
      Object.entries(this.statistics.countByTypeCode).forEach(([key, value]: [string, any]) => {
        if (!this.typeCodeMap[key]) {
          result[`Tipo ${key}`] = value.count;
        }
      });
    }

    return result;
  }

  /**
   * Get unknown status items for anomalies section
   */
  getUnknownStatusItems(): { [key: string]: number } {
    const result: { [key: string]: number } = {};

    if (this.statistics.countByStatus) {
      Object.entries(this.statistics.countByStatus).forEach(([key, value]: [string, any]) => {
        if (!this.statusCodeMap[key]) {
          result[`Estado ${key}`] = value;
        }
      });
    }

    return result;
  }

  /**
   * Get unknown source items for anomalies section
   */
  getUnknownSourceItems(): { [key: string]: number } {
    const result: { [key: string]: number } = {};

    if (this.statistics.countBySource) {
      Object.entries(this.statistics.countBySource).forEach(([key, value]: [string, any]) => {
        if (!this.sourceCodeMap[key]) {
          result[`Origen ${key}`] = value;
        }
      });
    }

    return result;
  }

  /**
   * Toggle anomalies section visibility
   */
  toggleAnomalies(): void {
    this.showAnomalies = !this.showAnomalies;
  }

  /**
   * Search contracts
   */
  searchContracts(): void {
    const searchTerm = this.searchControl.value;
    if (searchTerm) {
      this.router.navigate(['/contracts'], { 
        queryParams: { search: searchTerm } 
      });
    }
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
}
