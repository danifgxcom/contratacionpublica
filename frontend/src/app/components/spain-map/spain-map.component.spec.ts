import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SpainMapComponent } from './spain-map.component';

describe('SpainMapComponent', () => {
  let component: SpainMapComponent;
  let fixture: ComponentFixture<SpainMapComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SpainMapComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SpainMapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
