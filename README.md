# RDP Plugin for openfire

This plugin enables Openfire to become an RDP gateway for remote desktop control

There are two use cases for remote desktop control

1. Remote Assistance
2. Remote Desktop Control.

The first use case is available from a video conferencing session in Openfire Meetings using Pàdé. See [this issue](https://github.com/igniterealtime/Pade/issues/24) for details

The second use case is where a remote user can login remotely to fully control another computer using a protocol supported by the O/S like RDP for Windows.

This plugin enables the second use case for Pàdé users. Provided the remote computer is on the same network as Openfire and RDP is enabled, then the plugin will act as a gateway bridging RDP to HTML. See [this blog](https://www.brianmadden.com/opinion/How-HTML5-remote-desktop-clients-work) for technical details on how this works.
