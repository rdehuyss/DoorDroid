class DoorPiCard extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
    }

    set hass(hass) {
        if(this.notYetInitialized()) {
            this.initJsSIPIfNecessary(hass);
            this.initCameraView(hass);
        } else if(this.cameraEntityHasNewAccessToken(hass)) {
            this.updateCameraView(hass);
        }
        this.initDoorbellRinging(hass)
    }

    setConfig(config) {
        if (!config.camera_entity) {
            throw new Error('You need to define a camera entity');
        }
        if(!config.sip_settings) {
            throw new Error('You need to define the SIP settings');
        } else {
            if(!config.sip_settings.sip_wss_url) throw new Error('You need to define the SIP Secure Webservice url');
            if(!config.sip_settings.sip_server) throw new Error('You need to define the SIP Server (ip or hostname)');
            if(!config.sip_settings.sip_username) throw new Error('You need to define the SIP username');
            if(!config.sip_settings.sip_password) throw new Error('You need to define the SIP password');
        }
        this.config = config;

        const root = this.shadowRoot;
        if (root.lastChild) root.removeChild(root.lastChild);


        const card = document.createElement('ha-card');
        const content = document.createElement('div');
        const style = document.createElement('style');
        style.textContent = `
            ha-card {
                /* sample css */
            }
            .buttons {
                overflow: auto;
                padding: 16px;
                text-align: right;
            } 
            .ring-buttons {
                float: left;
                background: var(--paper-dialog-background-color, var(--primary-background-color));
                color: var(--paper-dialog-color, var(--primary-text-color));
            }
            #cameraview img{
                object-fit: cover;
                height: 400px;
            }
            mwc-button {
                margin-right: 16px;
            }
            `;
        content.innerHTML = `
        <div id='cameraview'>
            <p style="padding: 16px">Initializing SIP connection and webcam view</p>
            <audio id='audio-player'></audio>
        </div>
        <div class='buttons'>
            <div class="ring-buttons">
                <mwc-button dense id='bell-everywhere'><ha-icon class="mdc-button__icon" icon="mdi:bell-ring-outline"></ha-icon></mwc-button>
                <mwc-button dense id='bell-firstfloor'><ha-icon class="mdc-button__icon" icon="mdi:bell-outline"></ha-icon></mwc-button>
                <mwc-button dense id='bell-off'><ha-icon class="mdc-button__icon" icon="mdi:bell-off-outline"></ha-icon></mwc-button>
            </div>
            <mwc-button raised id='btn-open-door'>` + 'Open Door' + `</mwc-button>
            <mwc-button style='display:none' raised id='btn-accept-call'>` + 'Accept' + `</mwc-button>
            <mwc-button style='display:none' raised id='btn-reject-call'>` + 'Reject' + `</mwc-button>
            <mwc-button style='display:none' raised id='btn-end-call'>` + 'End' + `</mwc-button>
        </div>
        `;
        card.header = ''
        card.appendChild(content);
        card.appendChild(style);
        root.appendChild(card);
    }

    // The height of your card. Home Assistant uses this to automatically
    // distribute all cards over the available columns.
    getCardSize() {
        return 3;
    }

    notYetInitialized() {
        return window.JsSIP && !this.sipPhone && this.config;
    }

    initJsSIPIfNecessary(hass) {
        console.log('Loading SIPPhone');

        let socket = new JsSIP.WebSocketInterface(this.config.sip_settings.sip_wss_url);
        let configuration = {
            sockets  : [ socket ],
            uri      : `sip:${this.config.sip_settings.sip_username}@${this.config.sip_settings.sip_server}`,
            password : this.config.sip_settings.sip_password
        };
        this.sipPhone = new JsSIP.UA(configuration);
        this.sipPhone.start()


        let callOptions = { mediaConstraints: { audio: true, video: false } };// only audio calls

        this.sipPhone.on("registered", () => console.log('SIPPhone registered with SIP Server'));

        let droidCard = this;
        this.sipPhone.on("newRTCSession", function(data){
            let session = data.session; 

            
            if (session.direction === "incoming") {
                let acceptCallBtn = droidCard.getElementById('btn-accept-call');
                let rejectCallBtn = droidCard.getElementById('btn-reject-call');
                let endCallBtn = droidCard.getElementById('btn-end-call');

                session.on("accepted", () => {
                    console.log('call accepted')
                    acceptCallBtn.style.display = 'none';
                    rejectCallBtn.style.display = 'none';
                    endCallBtn.style.display = 'inline-flex';

                });
                session.on("confirmed", () => console.log('call confirmed'));
                session.on("ended", () => {console.log('call ended'); droidCard.cleanup(hass)});
                session.on("failed", () =>{console.log('call failed'); droidCard.cleanup(hass)});
                session.on("peerconnection", () => {
                    session.connection.addEventListener("addstream", (e) => {
                        console.log('adding audiostream')
                        // set remote audio stream (to listen to remote audio)
                        // remoteAudio is <audio> element on page
                        const remoteAudio = document.createElement('audio');
                        //const remoteAudio = droidCard.getElementById('audio-player');
                        remoteAudio.srcObject = e.stream;
                        remoteAudio.play();
                    })
                });
                
                acceptCallBtn.addEventListener('click', () => session.answer(callOptions));
                rejectCallBtn.addEventListener('click', () => session.terminate());
                endCallBtn.addEventListener('click', () => session.terminate());
                acceptCallBtn.style.display = 'inline-flex';
                rejectCallBtn.style.display = 'inline-flex';
            }
        });
    }

    initCameraView(hass) {
        this.cameraViewerShownTimeout = window.setTimeout(() => this.isDoorPiNotShown() , 15000);
        const cameraView = this.getElementById('cameraview');
        const imgEl = document.createElement('img');
        const camera_entity = this.config.camera_entity;
        this.access_token = hass.states[camera_entity].attributes['access_token'];
        imgEl.src = `/api/camera_proxy_stream/${camera_entity}?token=${this.access_token}`;
        imgEl.style.width = '100%';
        while (cameraView.firstChild) {
            cameraView.removeChild(cameraView.firstChild);
        }
        cameraView.appendChild(imgEl);
        console.log('initialized camera view');
    }

    updateCameraView(hass) {
        const imgEl = this.shadowRoot.querySelector('#cameraview img');
        const camera_entity = this.config.camera_entity;
        this.access_token = hass.states[camera_entity].attributes['access_token'];
        imgEl.src = `/api/camera_proxy_stream/${camera_entity}?token=${this.access_token}`;
    }

    cameraEntityHasNewAccessToken(hass) {
        clearTimeout(this.cameraViewerShownTimeout);
        this.cameraViewerShownTimeout = window.setTimeout(() => this.isDoorPiNotShown() , 15000);

        if(!this.sipPhone) return false;
        const old_access_token = this.access_token;
        const new_access_token = hass.states[this.config.camera_entity].attributes['access_token'];

        return old_access_token !== new_access_token;
    }

    initDoorbellRinging(hass) {
        this.setupBellRingingButton(hass, 'everywhere', 'firstfloor');
        this.setupBellRingingButton(hass, 'firstfloor', 'off');
        this.setupBellRingingButton(hass, 'off', 'everywhere');
    }

    setupBellRingingButton(hass, where, next) {
        let btn = this.shadowRoot.querySelector(`#bell-${where}`);

        if(hass.states['input_select.doorbell'].state == `ring-${where}`) {
            btn.style.display = 'inline-flex'; 
        } else {
            btn.style.display = 'none';
        }
        btn.addEventListener('click', () => hass.callService('input_select', 'select_option', { entity_id: 'input_select.doorbell', option: `ring-${next}`}));
    }

    isDoorPiNotShown() {
        const imgEl = this.shadowRoot.querySelector('#cameraview img');
        if(!this.isVisible(imgEl)) {
            this.stopCameraStreaming();
        }
    }

    stopCameraStreaming() {
        console.log('Stopping camera stream...');
        const imgEl = this.shadowRoot.querySelector('#cameraview img');
        imgEl.src = '';
        this.access_token = undefined;
    }

    isVisible(el) {
        if (!el.offsetParent && el.offsetWidth === 0 && el.offsetHeight === 0) {
            return false;
        }
        return true;
    }

    cleanup(hass) {
        let acceptCallBtn = this.getElementById('btn-accept-call');
        let rejectCallBtn = this.getElementById('btn-reject-call');
        let endCallBtn = this.getElementById('btn-end-call');

        //acceptCallBtn remove eventlisteners and hide
        let clonedAcceptCallBtn = acceptCallBtn.cloneNode(true)
        clonedAcceptCallBtn.style.display = 'none';
        acceptCallBtn.parentNode.replaceChild(clonedAcceptCallBtn, acceptCallBtn);

        //rejectCallBtn remove eventlisteners and hide
        let clonedRejectCallBtn = rejectCallBtn.cloneNode(true)
        clonedRejectCallBtn.style.display = 'none';
        rejectCallBtn.parentNode.replaceChild(clonedRejectCallBtn, rejectCallBtn);

        //endCallBtn remove eventlisteners and hide
        let clonedEndCallBtn = endCallBtn.cloneNode(true)
        clonedEndCallBtn.style.display = 'none';
        endCallBtn.parentNode.replaceChild(clonedEndCallBtn, endCallBtn);

        hass.callService('input_boolean', 'turn_off', { entity_id: 'input_boolean.doorbell' })
    }
    

    getElementById(id) {
        return this.shadowRoot.querySelector(`#${id}`);
    }
}

customElements.define('doorpi-card', DoorPiCard);
