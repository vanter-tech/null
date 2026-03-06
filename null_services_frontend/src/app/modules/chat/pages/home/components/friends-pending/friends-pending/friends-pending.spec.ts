import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FriendsPending } from './friends-pending';

describe('FriendsPending', () => {
  let component: FriendsPending;
  let fixture: ComponentFixture<FriendsPending>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FriendsPending]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FriendsPending);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
