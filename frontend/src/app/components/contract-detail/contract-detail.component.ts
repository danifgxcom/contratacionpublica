import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ContractService } from '../../services/contract.service';
import { Location } from '@angular/common';

@Component({
  selector: 'app-contract-detail',
  templateUrl: './contract-detail.component.html',
  styleUrls: ['./contract-detail.component.css']
})
export class ContractDetailComponent implements OnInit {
  contract: any = {};
  loading = true;
  error = false;
  showRawXml = false;

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
    private route: ActivatedRoute,
    private contractService: ContractService,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.loadContract();
  }

  loadContract(): void {
    this.loading = true;
    this.error = false;
    const id = this.route.snapshot.paramMap.get('id');
    
    if (id) {
      this.contractService.getContractById(id).subscribe({
        next: (data) => {
          this.contract = data;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error loading contract', err);
          this.error = true;
          this.loading = false;
        }
      });
    } else {
      this.error = true;
      this.loading = false;
    }
  }

  goBack(): void {
    this.location.back();
  }

  toggleRawXml(): void {
    this.showRawXml = !this.showRawXml;
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
}