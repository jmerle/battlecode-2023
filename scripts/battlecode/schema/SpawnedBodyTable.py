# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
np = import_numpy()

# A list of new bodies to be placed on the map.
class SpawnedBodyTable(object):
    __slots__ = ['_tab']

    @classmethod
    def GetRootAs(cls, buf, offset=0):
        n = flatbuffers.encode.Get(flatbuffers.packer.uoffset, buf, offset)
        x = SpawnedBodyTable()
        x.Init(buf, n + offset)
        return x

    @classmethod
    def GetRootAsSpawnedBodyTable(cls, buf, offset=0):
        """This method is deprecated. Please switch to GetRootAs."""
        return cls.GetRootAs(buf, offset)
    # SpawnedBodyTable
    def Init(self, buf, pos):
        self._tab = flatbuffers.table.Table(buf, pos)

    # The numeric ID of the new bodies.
    # Will never be negative.
    # There will only be one body with a particular ID at a time.
    # So, there will never be two robots with the same ID, or a robot and
    # a building with the same ID.
    # SpawnedBodyTable
    def RobotIds(self, j):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            a = self._tab.Vector(o)
            return self._tab.Get(flatbuffers.number_types.Int32Flags, a + flatbuffers.number_types.UOffsetTFlags.py_type(j * 4))
        return 0

    # SpawnedBodyTable
    def RobotIdsAsNumpy(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            return self._tab.GetVectorAsNumpy(flatbuffers.number_types.Int32Flags, o)
        return 0

    # SpawnedBodyTable
    def RobotIdsLength(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            return self._tab.VectorLen(o)
        return 0

    # SpawnedBodyTable
    def RobotIdsIsNone(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        return o == 0

    # The teams of the new bodies.
    # SpawnedBodyTable
    def TeamIds(self, j):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            a = self._tab.Vector(o)
            return self._tab.Get(flatbuffers.number_types.Int8Flags, a + flatbuffers.number_types.UOffsetTFlags.py_type(j * 1))
        return 0

    # SpawnedBodyTable
    def TeamIdsAsNumpy(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            return self._tab.GetVectorAsNumpy(flatbuffers.number_types.Int8Flags, o)
        return 0

    # SpawnedBodyTable
    def TeamIdsLength(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            return self._tab.VectorLen(o)
        return 0

    # SpawnedBodyTable
    def TeamIdsIsNone(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        return o == 0

    # The types of the new bodies.
    # SpawnedBodyTable
    def Types(self, j):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        if o != 0:
            a = self._tab.Vector(o)
            return self._tab.Get(flatbuffers.number_types.Int8Flags, a + flatbuffers.number_types.UOffsetTFlags.py_type(j * 1))
        return 0

    # SpawnedBodyTable
    def TypesAsNumpy(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        if o != 0:
            return self._tab.GetVectorAsNumpy(flatbuffers.number_types.Int8Flags, o)
        return 0

    # SpawnedBodyTable
    def TypesLength(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        if o != 0:
            return self._tab.VectorLen(o)
        return 0

    # SpawnedBodyTable
    def TypesIsNone(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        return o == 0

    # The locations of the bodies.
    # SpawnedBodyTable
    def Locs(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(10))
        if o != 0:
            x = self._tab.Indirect(o + self._tab.Pos)
            from battlecode.schema.VecTable import VecTable
            obj = VecTable()
            obj.Init(self._tab.Bytes, x)
            return obj
        return None

def SpawnedBodyTableStart(builder): builder.StartObject(4)
def Start(builder):
    return SpawnedBodyTableStart(builder)
def SpawnedBodyTableAddRobotIds(builder, robotIds): builder.PrependUOffsetTRelativeSlot(0, flatbuffers.number_types.UOffsetTFlags.py_type(robotIds), 0)
def AddRobotIds(builder, robotIds):
    return SpawnedBodyTableAddRobotIds(builder, robotIds)
def SpawnedBodyTableStartRobotIdsVector(builder, numElems): return builder.StartVector(4, numElems, 4)
def StartRobotIdsVector(builder, numElems):
    return SpawnedBodyTableStartRobotIdsVector(builder, numElems)
def SpawnedBodyTableAddTeamIds(builder, teamIds): builder.PrependUOffsetTRelativeSlot(1, flatbuffers.number_types.UOffsetTFlags.py_type(teamIds), 0)
def AddTeamIds(builder, teamIds):
    return SpawnedBodyTableAddTeamIds(builder, teamIds)
def SpawnedBodyTableStartTeamIdsVector(builder, numElems): return builder.StartVector(1, numElems, 1)
def StartTeamIdsVector(builder, numElems):
    return SpawnedBodyTableStartTeamIdsVector(builder, numElems)
def SpawnedBodyTableAddTypes(builder, types): builder.PrependUOffsetTRelativeSlot(2, flatbuffers.number_types.UOffsetTFlags.py_type(types), 0)
def AddTypes(builder, types):
    return SpawnedBodyTableAddTypes(builder, types)
def SpawnedBodyTableStartTypesVector(builder, numElems): return builder.StartVector(1, numElems, 1)
def StartTypesVector(builder, numElems):
    return SpawnedBodyTableStartTypesVector(builder, numElems)
def SpawnedBodyTableAddLocs(builder, locs): builder.PrependUOffsetTRelativeSlot(3, flatbuffers.number_types.UOffsetTFlags.py_type(locs), 0)
def AddLocs(builder, locs):
    return SpawnedBodyTableAddLocs(builder, locs)
def SpawnedBodyTableEnd(builder): return builder.EndObject()
def End(builder):
    return SpawnedBodyTableEnd(builder)