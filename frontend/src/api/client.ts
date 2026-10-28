import axios from 'axios'
import type { ApiError } from '../types'
import { toast } from 'sonner'
export const api=axios.create({baseURL:'/api',headers:{'Content-Type':'application/json'}})
const fallback:Record<number,string>={400:'Revisa los datos ingresados.',404:'No se encontró el registro solicitado.',409:'No se puede completar porque existen datos relacionados.',500:'Ocurrió un error interno. Inténtalo de nuevo.'}
api.interceptors.response.use(response=>response,error=>{const status:number|undefined=error.response?.status;const data=error.response?.data as ApiError|undefined;let message=data?.message||fallback[status||0]||'No fue posible conectar con el servidor.';if(status===409&&!data?.message)message=fallback[409];toast.error(message);return Promise.reject(error)})
