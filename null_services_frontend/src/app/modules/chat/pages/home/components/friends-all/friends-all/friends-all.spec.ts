import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FriendsAll } from './friends-all';

describe('FriendsAll', () => {
  let component: FriendsAll;
  let fixture: ComponentFixture<FriendsAll>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FriendsAll]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FriendsAll);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
