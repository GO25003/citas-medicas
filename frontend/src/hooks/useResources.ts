import { useQuery } from '@tanstack/react-query'
import { resources } from '../api/resources'

export const useEspecialidades=()=>useQuery({queryKey:['especialidades'],queryFn:resources.especialidades.list})
export const usePacientes=()=>useQuery({queryKey:['pacientes'],queryFn:resources.pacientes.list})
export const useMedicos=()=>useQuery({queryKey:['medicos'],queryFn:resources.medicos.list})
export const useDisponibilidades=()=>useQuery({queryKey:['disponibilidades'],queryFn:resources.disponibilidades.list})
export const useCitas=()=>useQuery({queryKey:['citas'],queryFn:resources.citas.list})
