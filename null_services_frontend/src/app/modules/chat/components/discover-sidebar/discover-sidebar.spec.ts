import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscoverSidebar } from './discover-sidebar';

describe('DiscoverSidebar', () => {
  let component: DiscoverSidebar;
  let fixture: ComponentFixture<DiscoverSidebar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DiscoverSidebar]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DiscoverSidebar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
