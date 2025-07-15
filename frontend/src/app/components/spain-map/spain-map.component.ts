import { Component, OnInit, AfterViewInit } from '@angular/core';
import { ContractService } from '../../services/contract.service';
import * as L from 'leaflet';

@Component({
  selector: 'app-spain-map',
  standalone: true,
  imports: [],
  templateUrl: './spain-map.component.html',
  styleUrl: './spain-map.component.css'
})
export class SpainMapComponent implements OnInit, AfterViewInit {
  private map!: L.Map;
  private canariasMap!: L.Map;
  private geoJsonLayer!: L.GeoJSON;
  private canariasGeoJsonLayer!: L.GeoJSON;
  private contractStats: any[] = [];
  private contractStatsByRegion: { [key: string]: any } = {};

  constructor(private contractService: ContractService) {}

  ngOnInit(): void {
    this.loadContractStatistics();
  }

  ngAfterViewInit(): void {
    // Add a small delay to ensure the container is fully rendered
    setTimeout(() => {
      this.initializeMap();
    }, 100);
  }

  private initializeMap(): void {
    console.log('Initializing map...');

    // Check if the container exists
    const container = document.getElementById('spain-map');
    if (!container) {
      console.error('Map container not found!');
      return;
    }

    console.log('Map container found:', container);

    try {
      // Initialize the map
      this.map = L.map('spain-map', {
        center: [40.4168, -3.7038], // Madrid center
        zoom: 6,
        minZoom: 5,
        maxZoom: 10,
        zoomControl: false,
        attributionControl: true,
        dragging: false,
        scrollWheelZoom: false,
        doubleClickZoom: false,
        boxZoom: false
      });

      console.log('Map initialized successfully');

      // Create a plain white background for the political map
      L.tileLayer('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=', {
        attribution: 'Mapa político de España'
      }).addTo(this.map);

      console.log('Tile layer added to map');

      // Load GeoJSON data
      this.loadGeoJsonData();
    } catch (error) {
      console.error('Error initializing map:', error);
    }
  }

  private loadContractStatistics(): void {
    this.contractService.getStatisticsByAutonomousCommunity().subscribe({
      next: (stats) => {
        this.contractStats = stats;
        // Create lookup by region name with proper mapping
        this.contractStatsByRegion = stats.reduce((acc: any, stat: any) => {
          // Map backend region names to GeoJSON region names
          const regionMapping: { [key: string]: string } = {
            'Andalucía': 'Andalucía',
            'Aragón': 'Aragón',
            'Principado de Asturias': 'Principado de Asturias',
            'Illes Balears': 'Illes Balears',
            'Canarias': 'Canarias',
            'Cantabria': 'Cantabria',
            'Castilla-La Mancha': 'Castilla-La Mancha',
            'Castilla y León': 'Castilla y León',
            'Catalunya': 'Cataluña',
            'Extremadura': 'Extremadura',
            'Galicia': 'Galicia',
            'Comunidad de Madrid': 'Comunidad de Madrid',
            'Región de Murcia': 'Región de Murcia',
            'Comunidad Foral de Navarra': 'Comunidad Foral de Navarra',
            'País Vasco': 'País Vasco',
            'La Rioja': 'La Rioja',
            'Comunitat Valenciana': 'Comunitat Valenciana',
            'Ceuta': 'Ciudad Autónoma de Ceuta',
            'Melilla': 'Ciudad Autónoma de Melilla'
          };
          
          const mappedName = regionMapping[stat.name] || stat.name;
          acc[mappedName] = stat;
          return acc;
        }, {});

        console.log('Contract statistics loaded:', this.contractStatsByRegion);

        // If map is already loaded, update the styling
        if (this.geoJsonLayer) {
          this.updateMapStyling();
        }
      },
      error: (error) => {
        console.error('Error loading contract statistics:', error);
      }
    });
  }

  private loadGeoJsonData(): void {
    console.log('Loading GeoJSON data...');

    fetch('/assets/data/spain-communities.geojson')
      .then(response => {
        console.log('GeoJSON response received:', response.status);
        return response.json();
      })
      .then(geoJsonData => {
        console.log('GeoJSON data parsed successfully');

        try {
          // Filter features for peninsula map (exclude Canary Islands)
          const peninsulaFeatures = geoJsonData.features.filter((feature: any) => 
            feature.properties.acom_name !== 'Canarias'
          );
          
          // Filter features for Canary Islands map
          const canaryFeatures = geoJsonData.features.filter((feature: any) => 
            feature.properties.acom_name === 'Canarias'
          );

          // Create peninsula map
          this.geoJsonLayer = L.geoJSON({
            type: 'FeatureCollection',
            features: peninsulaFeatures
          } as any, {
            style: (feature) => this.getFeatureStyle(feature),
            onEachFeature: (feature, layer) => this.onEachFeature(feature, layer)
          }).addTo(this.map);

          // Fit peninsula map to bounds with maximum zoom and show North Africa
          if (this.geoJsonLayer.getBounds().isValid()) {
            const bounds = this.geoJsonLayer.getBounds();
            // Extend bounds slightly south to include North Africa (for Ceuta and Melilla)
            const extendedBounds = L.latLngBounds(
              [bounds.getSouth() - 0.3, bounds.getWest()], // Smaller extension south
              [bounds.getNorth(), bounds.getEast()]
            );
            this.map.fitBounds(extendedBounds, {
              padding: [-20, -20]  // Negative padding to zoom in more
            });
          }

          // Initialize Canary Islands map
          this.initializeCanaryMap(canaryFeatures);

          console.log('GeoJSON layer added to map');
        } catch (error) {
          console.error('Error creating GeoJSON layer:', error);
        }
      })
      .catch(error => {
        console.error('Error loading GeoJSON data:', error);
      });
  }

  private getFeatureStyle(feature: any): L.PathOptions {
    const regionName = feature.properties.acom_name || feature.properties.name;
    const stats = this.contractStatsByRegion[regionName];

    console.log('Feature:', regionName, 'Stats:', stats);

    if (!stats) {
      return {
        fillColor: '#e0e0e0',
        weight: 2.5,
        opacity: 1,
        color: '#666666',
        dashArray: '3',
        fillOpacity: 0.8
      };
    }

    // Color based on number of contracts
    const contractCount = stats.contractCount || 0;
    let color = '#e0e0e0';

    if (contractCount > 10000) {
      color = '#800026';
    } else if (contractCount > 5000) {
      color = '#bd0026';
    } else if (contractCount > 2000) {
      color = '#e31a1c';
    } else if (contractCount > 1000) {
      color = '#fc4e2a';
    } else if (contractCount > 500) {
      color = '#fd8d3c';
    } else if (contractCount > 100) {
      color = '#feb24c';
    } else if (contractCount > 50) {
      color = '#fed976';
    } else if (contractCount > 0) {
      color = '#ffeda0';
    }

    return {
      fillColor: color,
      weight: 2.5,
      opacity: 1,
      color: '#666666',
      dashArray: '',
      fillOpacity: 0.8
    };
  }

  private onEachFeature(feature: any, layer: L.Layer): void {
    const regionName = feature.properties.acom_name ?? feature.properties.name;
    const stats = this.contractStatsByRegion[regionName];

    let popupContent = `<strong>${regionName}</strong><br>`;

    if (stats) {
      popupContent += `
        <strong>Contratos:</strong> ${stats.contractCount?.toLocaleString() ?? 0}<br>
        <strong>Importe Total:</strong> ${this.formatCurrency(stats.totalAmount ?? 0)}<br>
        <strong>Importe Promedio:</strong> ${this.formatCurrency(stats.averageAmount ?? 0)}
      `;
    } else {
      popupContent += 'Sin datos de contratos';
    }

    layer.bindPopup(popupContent);

    // Add hover effects
    layer.on('mouseover', (e) => {
      const targetLayer = e.target;
      targetLayer.setStyle({
        weight: 4,
        color: '#333',
        dashArray: '',
        fillOpacity: 0.9,
        // Add a subtle shadow effect
        shadowColor: '#000',
        shadowBlur: 10,
        shadowOffsetX: 5,
        shadowOffsetY: 5
      });

      // Bring the hovered region to the front
      if (!L.Browser.ie && !L.Browser.opera && !L.Browser.edge) {
        targetLayer.bringToFront();
      }

      // Show the popup on hover
      layer.openPopup();
    });

    layer.on('mouseout', (e) => {
      this.geoJsonLayer.resetStyle(e.target);
      // Close the popup on mouseout
      layer.closePopup();
    });

    layer.on('click', (e) => {
      // Remove zoom functionality - maps are now fixed
      // this.map.fitBounds(e.target.getBounds());
    });
  }

  private initializeCanaryMap(canaryFeatures: any[]): void {
    try {
      // Check if the container exists
      const container = document.getElementById('canarias-map');
      if (!container) {
        console.error('Canary Islands map container not found!');
        return;
      }

      // Initialize the Canary Islands map
      this.canariasMap = L.map('canarias-map', {
        center: [28.0, -15.5], // Canary Islands center
        zoom: 7,
        minZoom: 6,
        maxZoom: 9,
        zoomControl: false,
        attributionControl: false,
        dragging: false,
        scrollWheelZoom: false,
        doubleClickZoom: false,
        boxZoom: false
      });

      // Create a plain white background for the Canary Islands map
      L.tileLayer('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=', {
        attribution: 'Canarias'
      }).addTo(this.canariasMap);

      // Add Canary Islands to the separate map
      this.canariasGeoJsonLayer = L.geoJSON({
        type: 'FeatureCollection',
        features: canaryFeatures
      } as any, {
        style: (feature) => this.getFeatureStyle(feature),
        onEachFeature: (feature, layer) => this.onEachFeature(feature, layer)
      }).addTo(this.canariasMap);

      // Fit Canary Islands map to bounds with more padding to reduce zoom
      if (this.canariasGeoJsonLayer.getBounds().isValid()) {
        this.canariasMap.fitBounds(this.canariasGeoJsonLayer.getBounds(), {
          padding: [20, 20]
        });
      }

      console.log('Canary Islands map initialized successfully');
    } catch (error) {
      console.error('Error initializing Canary Islands map:', error);
    }
  }

  private updateMapStyling(): void {
    if (this.geoJsonLayer) {
      this.geoJsonLayer.eachLayer((layer: any) => {
        const style = this.getFeatureStyle(layer.feature);
        layer.setStyle(style);
      });
    }
    
    if (this.canariasGeoJsonLayer) {
      this.canariasGeoJsonLayer.eachLayer((layer: any) => {
        const style = this.getFeatureStyle(layer.feature);
        layer.setStyle(style);
      });
    }
  }

  private formatCurrency(amount: number): string {
    if (amount === null || amount === undefined) {
      return 'N/A';
    }
    return new Intl.NumberFormat('es-ES', { 
      style: 'currency', 
      currency: 'EUR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0 
    }).format(amount);
  }
}
