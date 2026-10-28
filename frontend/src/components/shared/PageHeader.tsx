import type { ReactNode } from 'react'
export function PageHeader({title,description,action}:{title:string;description:string;action?:ReactNode}){return <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"><div><h2 className="text-2xl font-semibold tracking-tight">{title}</h2><p className="mt-1 text-sm text-slate-500">{description}</p></div>{action}</div>}
