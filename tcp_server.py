import socket,threading, time, random


class TcpServer:

    def __init__(self):
        self.host = '104.128.233.118'
        #self.host='127.0.0.1'
        self.port = 9999
        self.max_listen = 100
        self.header_length = 4
        self._create_server()


    def _create_server(self):
        sk = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sk.bind((self.host, self.port))
        sk.listen(self.max_listen)
        while True:
            print('Waiting for connection')
            tcs, addr = sk.accept()
            print('Client connected, addr:', addr, ', start send message thread')
            threading.Thread(target=self._send_message, args=(tcs, addr), daemon=True).start()


    def _send_message(self, tcs, addr):
        client = ''
        setting_flag = False
        try:
            header = tcs.recv(4)
            print('Client header:', header)
            time.sleep(1)
            if byte_to_int(header):
                print('Prepare to receive client, length:', byte_to_int(header))
                client = tcs.recv(byte_to_int(header))
                client = client.decode()
                setting_flag = True
                print('Client:', client, ', prepare to send message and start close thread')
                threading.Thread(target=self._close_conn, args=(tcs, addr), daemon=True).start()
            else:
                print('Client send wrong message, connection closed, addr:', addr)
                tcs.close()
        except Exception as err:
            print('Error while get client info, addr:', addr, ', info:', err)
            tcs.close()
        while setting_flag:
            try:
                random_number = str(random.randint(1000, 999999))
                header = int_to_byte(len(random_number))
                tcs.send(header)
                print('Send', random_number, 'to', client, ', client addr:', addr)
                tcs.send(random_number.encode())
                time.sleep(5)
            except Exception as err2:
                print('Message send error, client:', client, ', info:', err2)
                tcs.close()
                break


    def _close_conn(self, tcs, addr):
        try:
            header = tcs.recv(4)
            if header:
                msg = tcs.recv(byte_to_int(header))
                if (msg.decode() == '0'):
                    tcs.close()
                    print('Connection closed by code, addr:', addr)
        except Exception as err:
            print('Error while receive message from', addr, ', info:', err)


def int_to_byte(i):
    ''' int to byte'''
    return ('0'*(4-len(str(i)))+str(i)).encode()


def byte_to_int(bytearr):
    ''' byte to int'''
    return int(bytearr.decode())


if __name__ == '__main__':
    TcpServer()
