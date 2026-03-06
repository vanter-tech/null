import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FriendAdd } from './friend-add';

describe('FriendAdd', () => {
  let component: FriendAdd;
  let fixture: ComponentFixture<FriendAdd>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FriendAdd]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FriendAdd);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
