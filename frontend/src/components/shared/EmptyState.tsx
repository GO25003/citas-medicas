import type { ReactNode } from 'react'
import { Inbox } from 'lucide-react'
export function EmptyState({title='No hay registros',description='Prueba con otra búsqueda o agrega un nuevo registro.',icon,action}:{title?:string;description?:string;icon?:ReactNode;action?:ReactNode}){return <div className="flex flex-col items-center justify-center px-6 py-14 text-center">{icon||<Inbox className="text-slate-300" size={38}/>}<h3 className="mt-4 font-semibold">{title}</h3><p className="mt-1 max-w-sm text-sm text-slate-500">{description}</p>{action&&<div className="mt-5">{action}</div>}</div>}
