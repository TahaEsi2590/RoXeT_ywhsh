# ReNo VPN build notes

- Existing logos/assets are unchanged.
- Default theme remains dark.
- The top bar has a light/dark toggle.
- VPN connection uses sing-box libbox 1.13.14 through Android VpnService.
- The admin panel's stored raw configuration is passed to the VPN service.
- Debug APK is built by GitHub Actions.

Important: URI import supports the common VLESS, VMess, Shadowsocks, Trojan and Hysteria2 formats. Raw sing-box JSON is also accepted. Complex URI features may require a full sing-box JSON profile.

sing-box is GPLv3; review its license obligations before distributing the APK.
