import { Injectable } from '@angular/core';
import { RxStomp, RxStompConfig } from '@stomp/rx-stomp';


const rxStompConfig: RxStompConfig = {
  brokerURL: 'ws://localhost:8088/api/v1/ws',

  connectHeaders: {
  },

  heartbeatIncoming: 0,
  heartbeatOutgoing: 20000,

  reconnectDelay: 5000,

  debug: (msg: string): void => {
    console.log(new Date(), msg);
  },

}

@Injectable({
  providedIn: 'root',
})
export class Websocket {

  public rxStomp: RxStomp;

  constructor() {
    this.rxStomp = new RxStomp();
    this.rxStomp.configure(rxStompConfig);
    this.rxStomp.activate();
  }
  
}
