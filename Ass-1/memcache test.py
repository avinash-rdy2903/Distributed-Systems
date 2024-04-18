from pymemcache.client.base import Client
cl = Client(('localhost',8080))
cl.set('test',"python")
cl.close()