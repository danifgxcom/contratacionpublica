import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { HomeComponent } from './components/home/home.component';
import { ContractListComponent } from './components/contract-list/contract-list.component';
import { ContractDetailComponent } from './components/contract-detail/contract-detail.component';
import { StatisticsComponent } from './components/statistics/statistics.component';

import { NgChartsModule } from 'ng2-charts';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'contracts', component: ContractListComponent },
  { path: 'contracts/:id', component: ContractDetailComponent },
  { path: 'statistics', component: StatisticsComponent },
  { path: '**', redirectTo: '' }
];

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    ContractListComponent,
    ContractDetailComponent,
    StatisticsComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forRoot(routes),
    NgChartsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }