# Leaflet Map Integration for Autonomous Communities

## Overview
This document describes the changes made to integrate a Leaflet map showing contracts by autonomous communities in Spain.

## Changes Made

1. **Added SpainMapComponent to AppModule**
   - Imported `SpainMapComponent` in `app.module.ts`
   - Added `SpainMapComponent` to the imports array in the `@NgModule` decorator

## What to Expect

When the application is run, you should see a map of Spain on the home page with:
- Different regions colored based on the number of contracts
- A legend explaining the color coding
- Interactive features like hovering over regions to see details
- Clicking on regions to zoom in

## Technical Details

The integration uses:
- Leaflet.js for the interactive map
- GeoJSON data for Spain's autonomous communities
- Backend API endpoint for statistics by autonomous community

## Verification

To verify that the integration is working properly:
1. The map should appear in the "Mapa de Contratos por Comunidad Autónoma" section of the home page
2. Hovering over a region should show a popup with statistics
3. The colors of the regions should match the data from the backend
4. The legend should correctly explain the color coding

If the map doesn't appear or doesn't work as expected, check the browser console for errors.