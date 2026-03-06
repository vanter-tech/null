import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FriendsOnline } from './friends-online';

describe('FriendsOnline', () => {
  let component: FriendsOnline;
  let fixture: ComponentFixture<FriendsOnline>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FriendsOnline]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FriendsOnline);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
