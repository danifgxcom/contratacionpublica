# Contratación Pública Frontend

This is the frontend application for the Contratación Pública project, which allows users to view and analyze public procurement data from the Spanish government.

## Features

- **Dashboard**: Overview of key metrics and statistics
- **Contract Listing**: Search and filter contracts with pagination
- **Contract Details**: Detailed view of individual contracts
- **Advanced Statistics**: Charts and data analysis
- **Data Export**: Export contract data to CSV

## Prerequisites

- Node.js >= 18.7.0
- npm (comes with Node.js)

## Node.js Version

This project requires Node.js version 18.7.0 or higher. If you're using nvm (Node Version Manager), you can run:

```bash
nvm use
```

This will automatically use the Node.js version specified in the `.nvmrc` file.

If you don't have the required Node.js version installed, you can install it with:

```bash
nvm install 18.7.0
```

Or download it directly from the [Node.js website](https://nodejs.org/).

## Installation

```bash
npm install
```

## Development Server

```bash
npm start
```

This will start a development server at `http://localhost:4200/`.

## Build

```bash
npm run build
```

The build artifacts will be stored in the `dist/` directory.

## Project Structure

The project follows a standard Angular application structure:

- `src/app/components/`: Contains all the application components
  - `home/`: Dashboard component with summary statistics
  - `contract-list/`: Component for listing and filtering contracts
  - `contract-detail/`: Component for displaying detailed contract information
  - `statistics/`: Component for advanced statistics and charts
- `src/app/services/`: Contains services for API communication
  - `contract.service.ts`: Service for contract-related API calls

## API Integration

The frontend communicates with the backend through the following endpoints:

- `GET /api/contracts`: Get paginated list of contracts
- `GET /api/contracts/:id`: Get details of a specific contract
- `GET /api/contracts/search/title`: Search contracts by title
- `GET /api/contracts/search/contracting-party`: Search contracts by contracting party
- `GET /api/contracts/search/source`: Search contracts by source (perfiles or agregadas)
- `GET /api/contracts/statistics`: Get contract statistics
- `POST /api/batch/process-atom-files`: Process atom files
