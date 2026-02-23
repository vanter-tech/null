import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscoverServers } from './discover-servers';

describe('DiscoverServers', () => {
  let component: DiscoverServers;
  let fixture: ComponentFixture<DiscoverServers>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DiscoverServers]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DiscoverServers);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
