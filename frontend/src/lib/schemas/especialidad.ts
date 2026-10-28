import { z } from 'zod'
export const especialidadSchema=z.object({nombre:z.string().trim().min(1,'El nombre es obligatorio').max(100,'Máximo 100 caracteres'),descripcion:z.string().max(500,'Máximo 500 caracteres').nullable()})
